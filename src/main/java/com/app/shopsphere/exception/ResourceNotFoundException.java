package com.app.shopsphere.exception;

/**
 * Raised when a requested entity cannot be located in the persistence layer.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}