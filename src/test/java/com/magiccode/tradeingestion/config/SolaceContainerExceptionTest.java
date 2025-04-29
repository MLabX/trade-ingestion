package com.magiccode.tradeingestion.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class SolaceContainerExceptionTest {

    @ParameterizedTest
    @EnumSource(SolaceContainerException.ErrorType.class)
    void testExceptionCreationWithMessage(SolaceContainerException.ErrorType errorType) {
        String message = "Test message";
        SolaceContainerException exception = new SolaceContainerException(errorType, message);

        assertEquals(errorType, exception.getErrorType());
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @ParameterizedTest
    @EnumSource(SolaceContainerException.ErrorType.class)
    void testExceptionCreationWithMessageAndCause(SolaceContainerException.ErrorType errorType) {
        String message = "Test message";
        Throwable cause = new RuntimeException("Test cause");
        SolaceContainerException exception = new SolaceContainerException(errorType, message, cause);

        assertEquals(errorType, exception.getErrorType());
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testToString() {
        SolaceContainerException.ErrorType errorType = SolaceContainerException.ErrorType.CONTAINER_STARTUP_FAILED;
        String message = "Test message";
        SolaceContainerException exception = new SolaceContainerException(errorType, message);

        String expected = String.format("SolaceContainerException{errorType=%s, message='%s'}", 
            errorType, message);
        assertEquals(expected, exception.toString());
    }

    @Test
    void testErrorTypeValues() {
        SolaceContainerException.ErrorType[] values = SolaceContainerException.ErrorType.values();
        assertEquals(6, values.length);
        assertArrayEquals(new SolaceContainerException.ErrorType[] {
            SolaceContainerException.ErrorType.CONTAINER_STARTUP_FAILED,
            SolaceContainerException.ErrorType.CONTAINER_CONFIGURATION_FAILED,
            SolaceContainerException.ErrorType.CONTAINER_VALIDATION_FAILED,
            SolaceContainerException.ErrorType.CONTAINER_RESOURCE_ERROR,
            SolaceContainerException.ErrorType.CONTAINER_NETWORK_ERROR,
            SolaceContainerException.ErrorType.CONTAINER_SECURITY_ERROR
        }, values);
    }

    @Test
    void testErrorTypeValueOf() {
        assertEquals(SolaceContainerException.ErrorType.CONTAINER_STARTUP_FAILED,
            SolaceContainerException.ErrorType.valueOf("CONTAINER_STARTUP_FAILED"));
        assertEquals(SolaceContainerException.ErrorType.CONTAINER_CONFIGURATION_FAILED,
            SolaceContainerException.ErrorType.valueOf("CONTAINER_CONFIGURATION_FAILED"));
        assertEquals(SolaceContainerException.ErrorType.CONTAINER_VALIDATION_FAILED,
            SolaceContainerException.ErrorType.valueOf("CONTAINER_VALIDATION_FAILED"));
        assertEquals(SolaceContainerException.ErrorType.CONTAINER_RESOURCE_ERROR,
            SolaceContainerException.ErrorType.valueOf("CONTAINER_RESOURCE_ERROR"));
        assertEquals(SolaceContainerException.ErrorType.CONTAINER_NETWORK_ERROR,
            SolaceContainerException.ErrorType.valueOf("CONTAINER_NETWORK_ERROR"));
        assertEquals(SolaceContainerException.ErrorType.CONTAINER_SECURITY_ERROR,
            SolaceContainerException.ErrorType.valueOf("CONTAINER_SECURITY_ERROR"));
    }

    @Test
    void testExceptionInheritance() {
        SolaceContainerException exception = new SolaceContainerException(
            SolaceContainerException.ErrorType.CONTAINER_STARTUP_FAILED,
            "Test message"
        );

        assertTrue(exception instanceof RuntimeException);
    }
} 