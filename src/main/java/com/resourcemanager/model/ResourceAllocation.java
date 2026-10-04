package com.resourcemanager.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain model representing a resource allocation session/lease.
 */
public class ResourceAllocation {
    private final String allocationId;
    private final User user;
    private final Resource resource;
    private final LocalDateTime startTime;
    private volatile LocalDateTime endTime;
    private volatile AllocationStatus allocationStatus;
    private volatile double totalCost;

    public ResourceAllocation(String allocationId, User user, Resource resource, LocalDateTime startTime) {
        if (allocationId == null || allocationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Allocation ID cannot be null or empty.");
        }
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        if (resource == null) {
            throw new IllegalArgumentException("Resource cannot be null.");
        }
        this.allocationId = allocationId;
        this.user = user;
        this.resource = resource;
        this.startTime = startTime != null ? startTime : LocalDateTime.now();
        this.allocationStatus = AllocationStatus.ACTIVE;
        this.totalCost = 0.0;
    }

    public String getAllocationId() {
        return allocationId;
    }

    public User getUser() {
        return user;
    }

    public Resource getResource() {
        return resource;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public AllocationStatus getAllocationStatus() {
        return allocationStatus;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void completeAllocation(LocalDateTime endTime, double calculatedCost) {
        this.endTime = endTime != null ? endTime : LocalDateTime.now();
        this.totalCost = Math.max(0.0, calculatedCost);
        this.allocationStatus = AllocationStatus.COMPLETED;
    }

    public void cancelAllocation() {
        this.endTime = LocalDateTime.now();
        this.allocationStatus = AllocationStatus.CANCELLED;
    }

    public long getDurationInMinutes() {
        LocalDateTime end = (endTime != null) ? endTime : LocalDateTime.now();
        return Duration.between(startTime, end).toMinutes();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceAllocation that = (ResourceAllocation) o;
        return Objects.equals(allocationId, that.allocationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allocationId);
    }

    @Override
    public String toString() {
        return "ResourceAllocation{" +
                "allocationId='" + allocationId + '\'' +
                ", userId=" + user.getUserId() +
                ", resourceId=" + resource.getResourceId() +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status=" + allocationStatus +
                ", totalCost=$" + String.format("%.2f", totalCost) +
                '}';
    }
}
