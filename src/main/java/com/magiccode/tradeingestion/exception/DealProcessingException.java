package com.magiccode.tradeingestion.exception;

public class DealProcessingException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public DealProcessingException(String message) {
        super(message);
    }
    
    public DealProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // Java 21 pattern matching for instanceof
    public boolean isValidationError() {
        return getMessage() != null && getMessage().contains("validation");
    }
    
    public boolean isSystemError() {
        return getMessage() != null && getMessage().contains("system");
    }
} 