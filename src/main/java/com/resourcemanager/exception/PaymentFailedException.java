package com.resourcemanager.exception;

/**
 * Custom Exception thrown when payment processing fails for a resource allocation.
 */
public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message) {
        super(message);
    }
}
