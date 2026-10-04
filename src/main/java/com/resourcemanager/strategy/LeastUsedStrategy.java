package com.resourcemanager.strategy;

import com.resourcemanager.model.Resource;

import java.util.Comparator;
import java.util.List;

/**
 * Concrete Strategy: Selects the available resource with the lowest usage count.
 * Useful for load balancing wear-and-tear across resources (e.g. workstations, computers, chargers).
 */
public class LeastUsedStrategy implements AllocationStrategy {

    @Override
    public Resource selectResource(List<Resource> candidateResources) {
        if (candidateResources == null || candidateResources.isEmpty()) {
            return null;
        }

        return candidateResources.stream()
                .filter(Resource::isAvailable)
                .min(Comparator.comparingLong(Resource::getUsageCount)
                        .thenComparing(Resource::getResourceId))
                .orElse(null);
    }

    @Override
    public String getStrategyName() {
        return "Least Used Strategy";
    }
}
