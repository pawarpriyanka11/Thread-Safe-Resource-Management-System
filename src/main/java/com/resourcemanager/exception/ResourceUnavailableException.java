package com.resourcemanager.exception;

/**
 * Custom Exception thrown when a resource exists but is currently allocated or unavailable.
 */
public class ResourceUnavailableException extends RuntimeException {
    public ResourceUnavailableException(String message) {
        super(message);
    }
}
