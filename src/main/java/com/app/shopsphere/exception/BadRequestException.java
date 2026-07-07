package com.app.shopsphere.exception;

/**
 * Raised when a client request violates the expected business rules.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}