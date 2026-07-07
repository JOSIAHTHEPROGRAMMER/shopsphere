package com.app.shopsphere.dto.exception;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Data;

@Data
public class ValidationErrorResponse {

    private boolean success;
    private String message;
    private Map<String, String> errors;
    private LocalDateTime timestamp;

    public ValidationErrorResponse(Map<String, String> errors) {
        this.success = false;
        this.message = "Validation failed";
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
    }
}