package com.app.shopsphere.exception;

/**
 * Raised when authentication fails or a token is not accepted.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}