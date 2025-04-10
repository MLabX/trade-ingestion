package com.magiccode.tradeingestion.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    List<String> details
) {
    public static ErrorResponse of(int status, String error, String message, String path, List<String> details) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, details);
    }
    
    public static ErrorResponse of(int status, String error, String message, String path) {
        return of(status, error, message, path, null);
    }
    
    // Java 21 record pattern matching
    public boolean hasDetails() {
        return details != null && !details.isEmpty();
    }
    
    public boolean isClientError() {
        return status >= 400 && status < 500;
    }
    
    public boolean isServerError() {
        return status >= 500;
    }
} 