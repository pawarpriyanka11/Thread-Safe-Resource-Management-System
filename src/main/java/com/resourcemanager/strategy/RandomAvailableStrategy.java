package com.resourcemanager.strategy;

import com.resourcemanager.model.Resource;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Concrete Strategy: Randomly selects an available resource from candidates.
 * Uses ThreadLocalRandom for high-concurrency performance without contention.
 */
public class RandomAvailableStrategy implements AllocationStrategy {

    @Override
    public Resource selectResource(List<Resource> candidateResources) {
        if (candidateResources == null || candidateResources.isEmpty()) {
            return null;
        }

        List<Resource> available = candidateResources.stream()
                .filter(Resource::isAvailable)
                .collect(Collectors.toList());

        if (available.isEmpty()) {
            return null;
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(available.size());
        return available.get(randomIndex);
    }

    @Override
    public String getStrategyName() {
        return "Random Available Strategy";
    }
}
