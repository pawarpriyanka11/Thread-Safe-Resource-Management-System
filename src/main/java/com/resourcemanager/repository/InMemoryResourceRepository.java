package com.resourcemanager.repository;

import com.resourcemanager.model.AllocationStatus;
import com.resourcemanager.model.Resource;
import com.resourcemanager.model.ResourceAllocation;
import com.resourcemanager.model.ResourceType;
import com.resourcemanager.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-Safe In-Memory implementation of ResourceRepository.
 * Uses ConcurrentHashMap for lock-free read performance and bucket-level concurrency on updates.
 */
public class InMemoryResourceRepository implements ResourceRepository {
    private final Map<String, Resource> resourceMap = new ConcurrentHashMap<>();
    private final Map<String, User> userMap = new ConcurrentHashMap<>();
    private final Map<String, ResourceAllocation> allocationMap = new ConcurrentHashMap<>();

    @Override
    public void saveResource(Resource resource) {
        if (resource != null) {
            resourceMap.put(resource.getResourceId(), resource);
        }
    }

    @Override
    public Optional<Resource> findResourceById(String resourceId) {
        return Optional.ofNullable(resourceMap.get(resourceId));
    }

    @Override
    public boolean removeResource(String resourceId) {
        return resourceMap.remove(resourceId) != null;
    }

    @Override
    public List<Resource> findAllResources() {
        return new ArrayList<>(resourceMap.values());
    }

    @Override
    public List<Resource> findResourcesByType(ResourceType type) {
        return resourceMap.values().stream()
                .filter(r -> r.getResourceType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public List<Resource> findAvailableResourcesByType(ResourceType type) {
        return resourceMap.values().stream()
                .filter(r -> r.getResourceType() == type && r.isAvailable())
                .collect(Collectors.toList());
    }

    @Override
    public void saveUser(User user) {
        if (user != null) {
            userMap.put(user.getUserId(), user);
        }
    }

    @Override
    public Optional<User> findUserById(String userId) {
        return Optional.ofNullable(userMap.get(userId));
    }

    @Override
    public List<User> findAllUsers() {
        return new ArrayList<>(userMap.values());
    }

    @Override
    public void saveAllocation(ResourceAllocation allocation) {
        if (allocation != null) {
            allocationMap.put(allocation.getAllocationId(), allocation);
        }
    }

    @Override
    public Optional<ResourceAllocation> findAllocationById(String allocationId) {
        return Optional.ofNullable(allocationMap.get(allocationId));
    }

    @Override
    public Optional<ResourceAllocation> findActiveAllocationByResource(String resourceId) {
        return allocationMap.values().stream()
                .filter(a -> a.getResource().getResourceId().equals(resourceId) && a.getAllocationStatus() == AllocationStatus.ACTIVE)
                .findFirst();
    }

    @Override
    public List<ResourceAllocation> findAllAllocations() {
        return new ArrayList<>(allocationMap.values());
    }
}
