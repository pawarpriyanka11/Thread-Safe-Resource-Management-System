package com.resourcemanager.strategy;

import com.resourcemanager.model.Resource;

import java.util.List;

/**
 * Concrete Strategy: Selects the first available resource in the candidate list.
 * Fast O(1) selection strategy.
 */
public class FirstAvailableStrategy implements AllocationStrategy {

    @Override
    public Resource selectResource(List<Resource> candidateResources) {
        if (candidateResources == null || candidateResources.isEmpty()) {
            return null;
        }
        for (Resource resource : candidateResources) {
            if (resource != null && resource.isAvailable()) {
                return resource;
            }
        }
        return null;
    }

    @Override
    public String getStrategyName() {
        return "First Available Strategy";
    }
}
