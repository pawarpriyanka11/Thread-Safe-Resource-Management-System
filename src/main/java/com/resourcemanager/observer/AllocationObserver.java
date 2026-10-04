package com.resourcemanager.observer;

import com.resourcemanager.model.ResourceAllocation;

/**
 * Observer Pattern Interface to notify external components of allocation lifecycle events.
 */
public interface AllocationObserver {
    void onResourceAllocated(ResourceAllocation allocation);
    void onResourceReleased(ResourceAllocation allocation);
}
