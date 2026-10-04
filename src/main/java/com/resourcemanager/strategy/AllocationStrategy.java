package com.resourcemanager.strategy;

import com.resourcemanager.model.Resource;

import java.util.List;

/**
 * Strategy Pattern Interface for resource selection algorithms.
 * Allows switching resource selection policies at runtime without changing ResourceManager code.
 */
public interface AllocationStrategy {
    /**
     * Selects an appropriate resource from candidate resources based on strategy logic.
     *
     * @param candidateResources list of candidate resources (must be filtered for AVAILABLE state)
     * @return selected Resource or null if none available
     */
    Resource selectResource(List<Resource> candidateResources);

    /**
     * @return Human-readable name of the allocation strategy
     */
    String getStrategyName();
}
