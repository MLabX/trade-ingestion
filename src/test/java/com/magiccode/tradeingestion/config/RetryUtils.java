package com.magiccode.tradeingestion.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Utility class for retry operations with exponential backoff.
 */
public class RetryUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RetryUtils.class);

    /**
     * Executes an operation with retry and exponential backoff.
     *
     * @param operation The operation to execute
     * @param maxRetries Maximum number of retry attempts
     * @param initialDelay Initial delay between retries
     * @param maxDelay Maximum delay between retries
     * @param <T> Return type of the operation
     * @return Result of the operation
     * @throws SolaceContainerException if all retry attempts fail
     */
    public static <T> T executeWithRetry(
            Supplier<T> operation,
            int maxRetries,
            Duration initialDelay,
            Duration maxDelay) {
        
        int attempt = 0;
        Duration currentDelay = initialDelay;
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                attempt++;
                
                if (attempt < maxRetries) {
                    LOGGER.warn("Attempt {} failed: {}. Retrying in {} ms...", 
                        attempt, e.getMessage(), currentDelay.toMillis());
                    
                    try {
                        TimeUnit.MILLISECONDS.sleep(currentDelay.toMillis());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new SolaceContainerException(
                            SolaceContainerException.ErrorType.CONTAINER_RESOURCE_ERROR,
                            "Operation interrupted", ie);
                    }
                    
                    // Exponential backoff with jitter
                    currentDelay = Duration.ofMillis(
                        Math.min(currentDelay.toMillis() * 2, maxDelay.toMillis()));
                }
            }
        }

        // If the last exception was a SolaceContainerException, preserve its error type
        if (lastException instanceof SolaceContainerException) {
            throw (SolaceContainerException) lastException;
        }

        throw new SolaceContainerException(
            SolaceContainerException.ErrorType.CONTAINER_RESOURCE_ERROR,
            String.format("Operation failed after %d attempts", maxRetries),
            lastException);
    }

    /**
     * Executes a void operation with retry and exponential backoff.
     *
     * @param operation The operation to execute
     * @param maxRetries Maximum number of retry attempts
     * @param initialDelay Initial delay between retries
     * @param maxDelay Maximum delay between retries
     * @throws SolaceContainerException if all retry attempts fail
     */
    public static void executeWithRetry(
            Runnable operation,
            int maxRetries,
            Duration initialDelay,
            Duration maxDelay) {
        
        executeWithRetry(() -> {
            operation.run();
            return null;
        }, maxRetries, initialDelay, maxDelay);
    }
} 