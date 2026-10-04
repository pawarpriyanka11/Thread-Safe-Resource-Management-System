package com.resourcemanager.payment;

import com.resourcemanager.model.Resource;
import com.resourcemanager.model.User;

/**
 * Strategy for zero-cost / free resource allocations.
 */
public class FreePaymentStrategy implements PaymentStrategy {

    @Override
    public boolean processPayment(User user, Resource resource, double amount) {
        // Free resource allocation always succeeds without charging
        return true;
    }

    @Override
    public String getPaymentMode() {
        return "FREE_TIER";
    }
}
