package com.resourcemanager.exception;

/**
 * Custom Exception thrown when an allocation request parameters or state transition is invalid.
 */
public class InvalidAllocationException extends RuntimeException {
    public InvalidAllocationException(String message) {
        super(message);
    }
}
