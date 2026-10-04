package com.resourcemanager.exception;

/**
 * Custom Exception thrown when a user ID does not exist in the system.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
