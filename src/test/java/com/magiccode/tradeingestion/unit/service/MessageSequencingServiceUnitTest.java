package com.magiccode.tradeingestion.unit.service;

import com.magiccode.tradeingestion.exception.DealProcessingException;
import com.magiccode.tradeingestion.model.TestDeal;
import com.magiccode.tradeingestion.service.MessageSequencingService;
import com.magiccode.tradeingestion.service.RedisService;
import com.magiccode.tradeingestion.service.MessageStasher;
import com.magiccode.tradeingestion.service.validation.SequenceValidator;
import com.magiccode.tradeingestion.unit.LightweightUnitTest;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MessageSequencingService.
 * These tests verify the service's ability to:
 * - Process deals in sequence with proper locking
 * - Handle validation failures
 * - Manage distributed locks
 * - Retry stashed messages
 * - Track metrics
 */
class MessageSequencingServiceUnitTest extends LightweightUnitTest {

    // Mock dependencies
    @Mock
    private RedisService redisService;  // Handles Redis operations for distributed locking

    @Mock
    private MeterRegistry meterRegistry;  // Tracks metrics for monitoring

    @Mock
    private SequenceValidator sequenceValidator;  // Validates message sequence

    @Mock
    private MessageStasher messageStasher;  // Handles stashing out-of-sequence messages

    @Mock
    private Counter counter;  // Mock counter for metrics

    private MessageSequencingService sequencingService;
    private TestDeal testDeal;

    @BeforeEach
    void setUp() {
        // Initialize the service with mocked dependencies
        sequencingService = new MessageSequencingService(redisService, meterRegistry, sequenceValidator, messageStasher);
        testDeal = createTestDeal();
    }

    /**
     * Tests successful processing of a deal with proper sequence.
     * Verifies that:
     * 1. Lock is acquired and released
     * 2. Deal is validated
     * 3. Success metric is recorded
     * 4. Stashed messages are cleaned up
     */
    @Test
    void processWithSequence_Success() {
        // Arrange
        when(redisService.acquireLock(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        when(redisService.releaseLock(anyString(), anyString())).thenReturn(true);
        when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);

        // Act
        assertDoesNotThrow(() -> sequencingService.processWithSequence(testDeal));

        // Assert
        verify(sequenceValidator).validate(testDeal);
        verify(meterRegistry).counter("deal.sequence.processed.success", "dealId", testDeal.getDealId());
        verify(messageStasher).cleanup(testDeal.getDealId());
    }

    /**
     * Tests behavior when lock acquisition fails.
     * Verifies that:
     * 1. Appropriate exception is thrown
     * 2. Failure metric is recorded
     * 3. Processing is aborted
     */
    @Test
    void processWithSequence_LockAcquisitionFails_ThrowsException() {
        // Arrange
        when(redisService.acquireLock(anyString(), anyString(), any(Duration.class))).thenReturn(false);
        when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);

        // Act & Assert
        DealProcessingException exception = assertThrows(
            DealProcessingException.class,
            () -> sequencingService.processWithSequence(testDeal)
        );
        assertEquals("Could not acquire lock for deal: " + testDeal.getDealId(), exception.getMessage());
        verify(meterRegistry).counter("deal.sequence.lock.acquisition.failed", "dealId", testDeal.getDealId());
    }

    /**
     * Tests behavior when validation fails.
     * Verifies that:
     * 1. Lock is properly released even on failure
     * 2. Original exception is propagated
     * 3. Failure metric is recorded
     */
    @Test
    void processWithSequence_ValidationFails_ReleasesLock() {
        // Arrange
        when(redisService.acquireLock(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        when(redisService.releaseLock(anyString(), anyString())).thenReturn(true);
        when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
        doThrow(new DealProcessingException("Validation failed")).when(sequenceValidator).validate(any());

        // Act & Assert
        assertThrows(DealProcessingException.class, () -> sequencingService.processWithSequence(testDeal));
        verify(redisService).releaseLock(anyString(), anyString());
        verify(meterRegistry).counter("deal.sequence.processed.failed", "dealId", testDeal.getDealId());
    }

    /**
     * Tests the local lock management functionality.
     * Verifies that:
     * 1. Same lock is returned for same deal ID
     * 2. Different locks are returned for different deal IDs
     * This ensures thread safety at the JVM level
     */
    @Test
    void getDealLock_ReturnsSameLockForSameDealId() {
        // Act
        Lock lock1 = sequencingService.getDealLock("DEAL-001");
        Lock lock2 = sequencingService.getDealLock("DEAL-001");
        Lock lock3 = sequencingService.getDealLock("DEAL-002");

        // Assert
        assertSame(lock1, lock2, "Locks for the same deal ID should be the same instance");
        assertNotSame(lock1, lock3, "Locks for different deal IDs should be different instances");
    }

    /**
     * Tests successful retry of stashed messages.
     * Verifies that:
     * 1. Stashed messages are retrieved
     * 2. Each message is validated
     * 3. Processing is attempted with proper locking
     */
    @Test
    void retryStashedMessages_Success() {
        // Arrange
        String stashKey = "deal:stash:TEST-DEAL-001:1";
        when(redisService.keys(anyString())).thenReturn(Set.of(stashKey));
        when(messageStasher.retrieve(stashKey)).thenReturn(testDeal);
        when(redisService.acquireLock(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        when(redisService.releaseLock(anyString(), anyString())).thenReturn(true);
        when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);

        // Act
        sequencingService.retryStashedMessages();

        // Assert
        verify(messageStasher).retrieve(stashKey);
        verify(sequenceValidator).validate(testDeal);
    }

    /**
     * Tests handling of null messages during retry.
     * Verifies that:
     * 1. Null messages are handled gracefully
     * 2. Processing continues without validation
     * This ensures system resilience to data inconsistencies
     */
    @Test
    void retryStashedMessages_NullMessage_HandlesGracefully() {
        // Arrange
        String stashKey = "deal:stash:TEST-DEAL-001:1";
        when(redisService.keys(anyString())).thenReturn(Set.of(stashKey));
        when(messageStasher.retrieve(stashKey)).thenReturn(null);

        // Act
        assertDoesNotThrow(() -> sequencingService.retryStashedMessages());

        // Assert
        verify(messageStasher).retrieve(stashKey);
        verify(sequenceValidator, never()).validate(any());
    }

    /**
     * Creates a test deal with standard test values.
     * This method provides consistent test data across all test cases.
     */
    private TestDeal createTestDeal() {
        TestDeal deal = new TestDeal();
        deal.setDealId("TEST-DEAL-001");
        deal.setClientId("CLIENT001");
        deal.setInstrumentId("INST001");
        deal.setQuantity(new BigDecimal("100"));
        deal.setPrice(new BigDecimal("10.5"));
        deal.setCurrency("USD");
        deal.setStatus("NEW");
        deal.setEventType("CREATE");
        deal.setVersion(1L);
        deal.setDealDate(LocalDateTime.now());
        return deal;
    }
}
