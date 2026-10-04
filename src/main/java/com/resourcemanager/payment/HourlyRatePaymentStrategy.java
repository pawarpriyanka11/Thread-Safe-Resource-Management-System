package com.resourcemanager.payment;

import com.resourcemanager.model.Resource;
import com.resourcemanager.model.User;

/**
 * Strategy for charging per-hour rates.
 */
public class HourlyRatePaymentStrategy implements PaymentStrategy {
    private final String paymentGateway;

    public HourlyRatePaymentStrategy(String paymentGateway) {
        this.paymentGateway = paymentGateway != null ? paymentGateway : "DefaultGateway";
    }

    @Override
    public boolean processPayment(User user, Resource resource, double amount) {
        if (amount <= 0) {
            return true;
        }
        // Simulates payment transaction with gateway (Stripe/PayPal/Internal balance)
        System.out.println(String.format("[PaymentGateway: %s] Processing payment of $%.2f for User: %s (%s) on Resource: %s",
                paymentGateway, amount, user.getName(), user.getUserId(), resource.getResourceId()));
        return true;
    }

    @Override
    public String getPaymentMode() {
        return "HOURLY_BILLING (" + paymentGateway + ")";
    }
}
