package com.app.shopsphere.exception;

/**
 * Raised when an inactive product is referenced in a mutable workflow.
 */
public class ProductInactiveException extends RuntimeException {
    public ProductInactiveException(String message) {
        super(message);
    }
}