package com.resourcemanager.exception;

/**
 * Custom Exception thrown when a requested resource is not found in repository.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
