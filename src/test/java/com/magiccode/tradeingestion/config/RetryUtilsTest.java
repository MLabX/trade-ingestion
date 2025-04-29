package com.magiccode.tradeingestion.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class RetryUtilsTest {
    private static final Duration SHORT_DELAY = Duration.ofMillis(100);
    private static final Duration MEDIUM_DELAY = Duration.ofMillis(500);
    private static final Duration LONG_DELAY = Duration.ofMillis(1000);

    @Test
    void testSuccessfulOperation() {
        AtomicInteger counter = new AtomicInteger(0);
        String result = RetryUtils.executeWithRetry(
            () -> {
                counter.incrementAndGet();
                return "success";
            },
            3,
            SHORT_DELAY,
            MEDIUM_DELAY
        );

        assertEquals("success", result);
        assertEquals(1, counter.get());
    }

    @Test
    void testRetryUntilSuccess() {
        AtomicInteger counter = new AtomicInteger(0);
        String result = RetryUtils.executeWithRetry(
            () -> {
                if (counter.incrementAndGet() < 3) {
                    throw new RuntimeException("Temporary failure");
                }
                return "success";
            },
            5,
            SHORT_DELAY,
            MEDIUM_DELAY
        );

        assertEquals("success", result);
        assertEquals(3, counter.get());
    }

    @Test
    void testMaxRetriesExceeded() {
        AtomicInteger counter = new AtomicInteger(0);
        assertThrows(SolaceContainerException.class, () ->
            RetryUtils.executeWithRetry(
                () -> {
                    counter.incrementAndGet();
                    throw new RuntimeException("Permanent failure");
                },
                3,
                SHORT_DELAY,
                MEDIUM_DELAY
            )
        );

        assertEquals(3, counter.get());
    }

    @Test
    void testVoidOperationSuccess() {
        AtomicInteger counter = new AtomicInteger(0);
        RetryUtils.executeWithRetry(
            () -> counter.incrementAndGet(),
            3,
            SHORT_DELAY,
            MEDIUM_DELAY
        );

        assertEquals(1, counter.get());
    }

    @Test
    void testVoidOperationRetry() {
        AtomicInteger counter = new AtomicInteger(0);
        RetryUtils.executeWithRetry(
            () -> {
                if (counter.incrementAndGet() < 2) {
                    throw new RuntimeException("Temporary failure");
                }
            },
            3,
            SHORT_DELAY,
            MEDIUM_DELAY
        );

        assertEquals(2, counter.get());
    }

    @Test
    void testInterruptedException() {
        AtomicReference<Boolean> interrupted = new AtomicReference<>(false);
        Thread.currentThread().interrupt();

        assertThrows(SolaceContainerException.class, () ->
            RetryUtils.executeWithRetry(
                () -> {
                    interrupted.set(Thread.currentThread().isInterrupted());
                    throw new RuntimeException("Test");
                },
                3,
                SHORT_DELAY,
                MEDIUM_DELAY
            )
        );

        assertTrue(interrupted.get());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @Timeout(5)
    void testExponentialBackoff(int maxRetries) {
        AtomicInteger counter = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        assertThrows(SolaceContainerException.class, () ->
            RetryUtils.executeWithRetry(
                () -> {
                    counter.incrementAndGet();
                    throw new RuntimeException("Test");
                },
                maxRetries,
                SHORT_DELAY,
                LONG_DELAY
            )
        );

        long duration = System.currentTimeMillis() - startTime;
        long expectedMinDuration = SHORT_DELAY.toMillis() * (int)(Math.pow(2, maxRetries - 1) - 1);
        long expectedMaxDuration = LONG_DELAY.toMillis() * maxRetries;

        assertTrue(duration >= expectedMinDuration, 
            String.format("Duration %d ms should be at least %d ms", duration, expectedMinDuration));
        assertTrue(duration <= expectedMaxDuration,
            String.format("Duration %d ms should be at most %d ms", duration, expectedMaxDuration));
    }

    @Test
    void testErrorTypePreserved() {
        try {
            RetryUtils.executeWithRetry(
                () -> {
                    throw new SolaceContainerException(
                        SolaceContainerException.ErrorType.CONTAINER_NETWORK_ERROR,
                        "Network error"
                    );
                },
                3,
                SHORT_DELAY,
                MEDIUM_DELAY
            );
            fail("Expected SolaceContainerException");
        } catch (SolaceContainerException e) {
            assertEquals(SolaceContainerException.ErrorType.CONTAINER_NETWORK_ERROR, e.getErrorType());
        }
    }
} 