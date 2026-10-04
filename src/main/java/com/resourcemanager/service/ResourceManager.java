package com.resourcemanager.service;

import com.resourcemanager.exception.InvalidAllocationException;
import com.resourcemanager.exception.ResourceNotFoundException;
import com.resourcemanager.exception.ResourceUnavailableException;
import com.resourcemanager.exception.UserNotFoundException;
import com.resourcemanager.model.*;
import com.resourcemanager.observer.AllocationObserver;
import com.resourcemanager.repository.ResourceRepository;
import com.resourcemanager.strategy.AllocationStrategy;
import com.resourcemanager.strategy.FirstAvailableStrategy;

import java.time.LocalDateTime;

import java.util.List;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Core Business Logic Controller for Thread-Safe Resource Management.
 * Manages resource lifecycle, locking, strategies, and status transitions.
 */
public class ResourceManager {
    private final ResourceRepository repository;
    private final List<AllocationObserver> observers = new CopyOnWriteArrayList<>();
    private final AtomicLong allocationIdSequence = new AtomicLong(1000);
    private volatile AllocationStrategy allocationStrategy;

    public ResourceManager(ResourceRepository repository) {
        this(repository, new FirstAvailableStrategy());
    }

    public ResourceManager(ResourceRepository repository, AllocationStrategy strategy) {
        if (repository == null) {
            throw new IllegalArgumentException("Repository cannot be null.");
        }
        this.repository = repository;
        this.allocationStrategy = strategy != null ? strategy : new FirstAvailableStrategy();
    }

    public void setAllocationStrategy(AllocationStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Allocation strategy cannot be null.");
        }
        this.allocationStrategy = strategy;
    }

    public AllocationStrategy getAllocationStrategy() {
        return allocationStrategy;
    }

    public void addObserver(AllocationObserver observer) {
        if (observer != null) {
            observers.add(observer);
        }
    }

    public void removeObserver(AllocationObserver observer) {
        observers.remove(observer);
    }

    public void registerUser(User user) {
        repository.saveUser(user);
    }

    public User getUser(String userId) {
        return repository.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    public void registerResource(Resource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Resource cannot be null.");
        }
        repository.saveResource(resource);
    }

    public boolean removeResource(String resourceId) {
        Resource resource = repository.findResourceById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + resourceId));

        ReentrantLock lock = resource.getLock();
        lock.lock();
        try {
            if (resource.getStatus() == ResourceStatus.ALLOCATED) {
                throw new InvalidAllocationException("Cannot remove resource " + resourceId + " because it is currently ALLOCATED.");
            }
            return repository.removeResource(resourceId);
        } finally {
            lock.unlock();
        }
    }

    public ResourceStatus getResourceStatus(String resourceId) {
        return repository.findResourceById(resourceId)
                .map(Resource::getStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + resourceId));
    }

    /**
     * Finds an available resource of specified type using the current AllocationStrategy.
     */
    public Resource findAvailableResource(ResourceType type) {
        List<Resource> candidates = repository.findAvailableResourcesByType(type);
        if (candidates.isEmpty()) {
            return null;
        }
        return allocationStrategy.selectResource(candidates);
    }

    /**
     * Allocates a specific resource to a user with thread safety.
     * Demonstrates Fine-Grained Instance Locking (ReentrantLock) & Double-Check pattern.
     */
    public ResourceAllocation allocateResource(String userId, String resourceId) {
        User user = repository.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        Resource resource = repository.findResourceById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + resourceId));

        ReentrantLock lock = resource.getLock();
        lock.lock(); // Fine-grained lock per resource
        try {
            // Double-check availability under lock
            if (!resource.isAvailable()) {
                throw new ResourceUnavailableException("Resource " + resourceId + " is not available. Current status: " + resource.getStatus());
            }

            // State Transition
            resource.setStatus(ResourceStatus.ALLOCATED);
            resource.incrementUsageCount();

            String allocationId = "ALLOC-" + allocationIdSequence.incrementAndGet();
            ResourceAllocation allocation = new ResourceAllocation(allocationId, user, resource, LocalDateTime.now());

            repository.saveAllocation(allocation);
            notifyAllocated(allocation);

            return allocation;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Automatically selects an available resource matching type and allocates it to user.
     * Handles race conditions gracefully by checking locks and retrying if selected resource is snatched.
     */
    public ResourceAllocation allocateAvailableResource(String userId, ResourceType type) {
        User user = repository.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        int maxRetries = 10;
        for (int i = 0; i < maxRetries; i++) {
            List<Resource> candidates = repository.findAvailableResourcesByType(type);
            if (candidates.isEmpty()) {
                throw new ResourceUnavailableException("No available resources found for type: " + type);
            }

            Resource selected = allocationStrategy.selectResource(candidates);
            if (selected == null) {
                throw new ResourceUnavailableException("Allocation strategy returned no resource for type: " + type);
            }

            ReentrantLock lock = selected.getLock();
            if (lock.tryLock()) { // Non-blocking attempt to acquire lock
                try {
                    if (selected.isAvailable()) {
                        selected.setStatus(ResourceStatus.ALLOCATED);
                        selected.incrementUsageCount();

                        String allocationId = "ALLOC-" + allocationIdSequence.incrementAndGet();
                        ResourceAllocation allocation = new ResourceAllocation(allocationId, user, selected, LocalDateTime.now());

                        repository.saveAllocation(allocation);
                        notifyAllocated(allocation);

                        return allocation;
                    }
                } finally {
                    lock.unlock();
                }
            }
            // Retries if another thread snatched selected resource between query and lock
        }
        throw new ResourceUnavailableException("High contention: Unable to lock an available resource for type: " + type);
    }

    /**
     * Releases an allocated resource, setting state back to AVAILABLE and finalizing duration/cost.
     */
    public ResourceAllocation releaseResource(String allocationId) {
        ResourceAllocation allocation = repository.findAllocationById(allocationId)
                .orElseThrow(() -> new InvalidAllocationException("Allocation ID not found: " + allocationId));

        if (allocation.getAllocationStatus() != AllocationStatus.ACTIVE) {
            throw new InvalidAllocationException("Allocation " + allocationId + " is already " + allocation.getAllocationStatus());
        }

        Resource resource = allocation.getResource();
        ReentrantLock lock = resource.getLock();
        lock.lock();
        try {
            resource.setStatus(ResourceStatus.AVAILABLE);

            LocalDateTime endTime = LocalDateTime.now();
            long durationMinutes = Math.max(1, allocation.getDurationInMinutes());
            double calculatedCost = (durationMinutes / 60.0) * resource.getHourlyRate();

            allocation.completeAllocation(endTime, calculatedCost);
            notifyReleased(allocation);

            return allocation;
        } finally {
            lock.unlock();
        }
    }

    private void notifyAllocated(ResourceAllocation allocation) {
        for (AllocationObserver observer : observers) {
            try {
                observer.onResourceAllocated(allocation);
            } catch (Exception e) {
                System.err.println("Error notifying observer: " + e.getMessage());
            }
        }
    }

    private void notifyReleased(ResourceAllocation allocation) {
        for (AllocationObserver observer : observers) {
            try {
                observer.onResourceReleased(allocation);
            } catch (Exception e) {
                System.err.println("Error notifying observer: " + e.getMessage());
            }
        }
    }

    public ResourceRepository getRepository() {
        return repository;
    }
}
