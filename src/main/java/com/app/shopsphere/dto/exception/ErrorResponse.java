package com.app.shopsphere.dto.exception;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ErrorResponse {

    private boolean success;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponse(String message) {
        this.success = false;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}