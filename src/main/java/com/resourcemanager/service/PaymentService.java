package com.resourcemanager.service;

import com.resourcemanager.exception.PaymentFailedException;
import com.resourcemanager.model.Resource;
import com.resourcemanager.model.User;
import com.resourcemanager.payment.FreePaymentStrategy;
import com.resourcemanager.payment.PaymentStrategy;

/**
 * Service handling payment verification and execution (Extensible Payment Subsystem).
 */
public class PaymentService {
    private volatile PaymentStrategy defaultPaymentStrategy;

    public PaymentService() {
        this.defaultPaymentStrategy = new FreePaymentStrategy();
    }

    public PaymentService(PaymentStrategy defaultStrategy) {
        this.defaultPaymentStrategy = defaultStrategy != null ? defaultStrategy : new FreePaymentStrategy();
    }

    public void setDefaultPaymentStrategy(PaymentStrategy strategy) {
        if (strategy != null) {
            this.defaultPaymentStrategy = strategy;
        }
    }

    public boolean processPayment(User user, Resource resource, double estimatedHours) {
        return processPayment(user, resource, estimatedHours, defaultPaymentStrategy);
    }

    public boolean processPayment(User user, Resource resource, double estimatedHours, PaymentStrategy strategy) {
        if (strategy == null) {
            strategy = defaultPaymentStrategy;
        }
        double amount = Math.max(0.0, estimatedHours * resource.getHourlyRate());
        boolean success = strategy.processPayment(user, resource, amount);
        if (!success) {
            throw new PaymentFailedException(String.format("Payment failed for user %s on resource %s (Amount: $%.2f)",
                    user.getUserId(), resource.getResourceId(), amount));
        }
        return true;
    }
}
