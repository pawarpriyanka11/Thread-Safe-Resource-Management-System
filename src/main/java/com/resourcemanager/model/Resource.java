package com.resourcemanager.model;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Base abstract class representing a shared resource.
 * Demonstrates Object-Oriented Design (Abstraction, Inheritance, Encapsulation)
 * and Thread-Safety (ReentrantLock per resource instance, AtomicLong for analytics).
 *
 * Concurrency Design Rationale:
 * 1. ReentrantLock per instance ensures fine-grained synchronization.
 *    Allocating Resource A does NOT block operations on Resource B.
 * 2. AtomicLong provides lock-free, atomic thread-safe updates to usage counter.
 * 3. volatile status ensures immediate visibility of status changes across threads.
 */
public abstract class Resource {
    private final String resourceId;
    private final ResourceType resourceType;
    private final double hourlyRate;
    private final ReentrantLock lock;
    private final AtomicLong usageCount;
    private volatile ResourceStatus status;

    public Resource(String resourceId, ResourceType resourceType, double hourlyRate) {
        if (resourceId == null || resourceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Resource ID cannot be null or empty.");
        }
        if (resourceType == null) {
            throw new IllegalArgumentException("Resource type cannot be null.");
        }
        this.resourceId = resourceId;
        this.resourceType = resourceType;
        this.hourlyRate = Math.max(0.0, hourlyRate);
        this.status = ResourceStatus.AVAILABLE;
        this.lock = new ReentrantLock(true); // Fair lock to prevent thread starvation
        this.usageCount = new AtomicLong(0);
    }

    public String getResourceId() {
        return resourceId;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public ResourceStatus getStatus() {
        return status;
    }

    public void setStatus(ResourceStatus status) {
        this.status = status;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public long getUsageCount() {
        return usageCount.get();
    }

    public void incrementUsageCount() {
        usageCount.incrementAndGet();
    }

    public boolean isAvailable() {
        return status == ResourceStatus.AVAILABLE;
    }

    /**
     * Polymorphic method to return detailed info specific to each resource subtype.
     */
    public abstract String getDetails();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return Objects.equals(resourceId, resource.resourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceId);
    }

    @Override
    public String toString() {
        return "Resource{" +
                "resourceId='" + resourceId + '\'' +
                ", type=" + resourceType +
                ", status=" + status +
                ", usageCount=" + usageCount.get() +
                ", hourlyRate=$" + String.format("%.2f", hourlyRate) +
                '}';
    }
}
