package com.app.shopsphere.exception;

/**
 * Raised when an authenticated user attempts an action outside their role
 * scope.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}