package com.resourcemanager.payment;

import com.resourcemanager.model.Resource;
import com.resourcemanager.model.User;

/**
 * Strategy Pattern Interface for Payment Processing.
 * Allows making payment handling optional and extensible.
 */
public interface PaymentStrategy {
    /**
     * Processes payment for allocating a resource.
     *
     * @param user user making the allocation
     * @param resource target resource
     * @param amount calculated amount
     * @return true if payment succeeded, false otherwise
     */
    boolean processPayment(User user, Resource resource, double amount);

    String getPaymentMode();
}
