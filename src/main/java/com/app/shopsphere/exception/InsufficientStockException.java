package com.app.shopsphere.exception;

/**
 * Raised when a cart or order operation exceeds the available stock.
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}