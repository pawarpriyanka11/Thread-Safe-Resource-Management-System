package com.resourcemanager.service;

import com.resourcemanager.model.ResourceAllocation;
import com.resourcemanager.model.ResourceType;
import com.resourcemanager.payment.PaymentStrategy;

/**
 * Facade Service that orchestrates higher-level resource allocation workflows,
 * combining ResourceManager allocation logic with PaymentService payment validation.
 */
public class AllocationService {
    private final ResourceManager resourceManager;
    private final PaymentService paymentService;

    public AllocationService(ResourceManager resourceManager, PaymentService paymentService) {
        if (resourceManager == null) {
            throw new IllegalArgumentException("ResourceManager cannot be null.");
        }
        this.resourceManager = resourceManager;
        this.paymentService = paymentService != null ? paymentService : new PaymentService();
    }

    /**
     * Allocates a resource of specified type after validating payment requirements.
     */
    public ResourceAllocation allocateResourceWithPayment(String userId, ResourceType type, double estimatedHours, PaymentStrategy customPaymentStrategy) {
        var user = resourceManager.getUser(userId);
        var resourceCandidate = resourceManager.findAvailableResource(type);

        if (resourceCandidate != null && resourceCandidate.getHourlyRate() > 0) {
            paymentService.processPayment(user, resourceCandidate, estimatedHours, customPaymentStrategy);
        }

        return resourceManager.allocateAvailableResource(userId, type);
    }

    /**
     * Direct allocation of a specific resource by ID.
     */
    public ResourceAllocation allocateSpecificResource(String userId, String resourceId) {
        return resourceManager.allocateResource(userId, resourceId);
    }

    /**
     * Direct allocation of any available resource of a type.
     */
    public ResourceAllocation allocateAvailableResource(String userId, ResourceType type) {
        return resourceManager.allocateAvailableResource(userId, type);
    }

    /**
     * Releases an active allocation lease.
     */
    public ResourceAllocation releaseResource(String allocationId) {
        return resourceManager.releaseResource(allocationId);
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }
}
