package com.app.shopsphere.exception;

/**
 * Raised when an order moves through an invalid status transition.
 */
public class OrderStatusException extends RuntimeException {
    public OrderStatusException(String message) {
        super(message);
    }
}