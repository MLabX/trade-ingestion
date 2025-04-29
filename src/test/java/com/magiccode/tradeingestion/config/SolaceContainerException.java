package com.magiccode.tradeingestion.config;

/**
 * Exception thrown when there is an error with Solace container operations.
 */
public class SolaceContainerException extends RuntimeException {
    private final ErrorType errorType;

    public enum ErrorType {
        CONTAINER_STARTUP_FAILED,
        CONTAINER_CONFIGURATION_FAILED,
        CONTAINER_VALIDATION_FAILED,
        CONTAINER_RESOURCE_ERROR,
        CONTAINER_NETWORK_ERROR,
        CONTAINER_SECURITY_ERROR
    }

    public SolaceContainerException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public SolaceContainerException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String toString() {
        return String.format("SolaceContainerException{errorType=%s, message='%s'}", 
            errorType, getMessage());
    }
} 