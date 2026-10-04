package com.resourcemanager.observer;

import com.resourcemanager.model.ResourceAllocation;

import java.time.LocalDateTime;

/**
 * Audit Logger Observer that records lifecycle events for system compliance and monitoring.
 */
public class AuditLoggerObserver implements AllocationObserver {

    @Override
    public void onResourceAllocated(ResourceAllocation allocation) {
        System.out.println(String.format("[AUDIT LOG %s] [Thread: %s] ALLOCATED - AllocationID: %s | User: %s | Resource: %s (%s)",
                LocalDateTime.now(),
                Thread.currentThread().getName(),
                allocation.getAllocationId(),
                allocation.getUser().getUserId(),
                allocation.getResource().getResourceId(),
                allocation.getResource().getResourceType()));
    }

    @Override
    public void onResourceReleased(ResourceAllocation allocation) {
        System.out.println(String.format("[AUDIT LOG %s] [Thread: %s] RELEASED - AllocationID: %s | User: %s | Resource: %s | Duration: %d mins | Total Cost: $%.2f",
                LocalDateTime.now(),
                Thread.currentThread().getName(),
                allocation.getAllocationId(),
                allocation.getUser().getUserId(),
                allocation.getResource().getResourceId(),
                allocation.getDurationInMinutes(),
                allocation.getTotalCost()));
    }
}
