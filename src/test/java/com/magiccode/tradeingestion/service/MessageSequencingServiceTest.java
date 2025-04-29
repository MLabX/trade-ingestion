package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.exception.DealProcessingException;
import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.service.validation.SequenceValidator;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageSequencingServiceTest {

    @Mock
    private RedisService redisService;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private SequenceValidator sequenceValidator;

    @Mock
    private MessageStasher messageStasher;

    @InjectMocks
    private MessageSequencingService messageSequencingService;

    @Mock
    private Deal deal;
    private String dealId;
    private String lockKey;
    private String lockValue;

    @BeforeEach
    void setUp() {
        dealId = "TEST-DEAL-123";
        when(deal.getDealId()).thenReturn(dealId);
        when(deal.getEventType()).thenReturn("CREATED");
        when(deal.getVersion()).thenReturn(1L);

        lockKey = "deal:lock:" + dealId;
        lockValue = "test-thread:1234567890";
    }

    @Nested
    @DisplayName("getDealLock")
    class GetDealLockTests {
        @Test
        @DisplayName("should return new lock for new deal ID")
        void shouldReturnNewLockForNewDealId() {
            // When
            Lock lock = messageSequencingService.getDealLock(dealId);

            // Then
            assertNotNull(lock);
            assertTrue(lock instanceof ReentrantLock);
        }

        @Test
        @DisplayName("should return same lock for same deal ID")
        void shouldReturnSameLockForSameDealId() {
            // When
            Lock lock1 = messageSequencingService.getDealLock(dealId);
            Lock lock2 = messageSequencingService.getDealLock(dealId);

            // Then
            assertSame(lock1, lock2);
        }

        @Test
        @DisplayName("should return different locks for different deal IDs")
        void shouldReturnDifferentLocksForDifferentDealIds() {
            // When
            Lock lock1 = messageSequencingService.getDealLock("DEAL-1");
            Lock lock2 = messageSequencingService.getDealLock("DEAL-2");

            // Then
            assertNotSame(lock1, lock2);
        }
    }

    @Nested
    @DisplayName("processWithSequence")
    class ProcessWithSequenceTests {
        @Test
        @DisplayName("should successfully process deal when lock is acquired")
        void shouldSuccessfullyProcessDealWhenLockIsAcquired() {
            // Given
            when(redisService.acquireLock(anyString(), anyString(), any(Duration.class))).thenReturn(true);
            when(redisService.releaseLock(anyString(), anyString())).thenReturn(true);

            // When
            assertDoesNotThrow(() -> messageSequencingService.processWithSequence(deal));

            // Then
            verify(sequenceValidator).validate(deal);
            verify(meterRegistry).counter("deal.sequence.processed.success", "dealId", dealId);
        }

        @Test
        @DisplayName("should throw exception when lock acquisition fails")
        void shouldThrowExceptionWhenLockAcquisitionFails() {
            // Given
            when(redisService.acquireLock(anyString(), anyString(), any(Duration.class))).thenReturn(false);

            // When & Then
            DealProcessingException exception = assertThrows(
                DealProcessingException.class,
                () -> messageSequencingService.processWithSequence(deal)
            );
            assertEquals("Could not acquire lock for deal: " + dealId, exception.getMessage());
            verify(meterRegistry).counter("deal.sequence.lock.acquisition.failed", "dealId", dealId);
        }

        @Test
        @DisplayName("should release lock even when validation fails")
        void shouldReleaseLockEvenWhenValidationFails() {
            // Given
            when(redisService.acquireLock(anyString(), anyString(), any(Duration.class))).thenReturn(true);
            when(redisService.releaseLock(anyString(), anyString())).thenReturn(true);
            doThrow(new DealProcessingException("Validation failed")).when(sequenceValidator).validate(any());

            // When & Then
            assertThrows(DealProcessingException.class, () -> messageSequencingService.processWithSequence(deal));
            verify(redisService).releaseLock(lockKey, anyString());
        }

        @Test
        @DisplayName("should release lock even when processing fails")
        void shouldReleaseLockEvenWhenProcessingFails() {
            // Given
            when(redisService.acquireLock(anyString(), anyString(), any(Duration.class))).thenReturn(true);
            when(redisService.releaseLock(anyString(), anyString())).thenReturn(true);
            doThrow(new RuntimeException("Processing failed")).when(sequenceValidator).validate(any());

            // When & Then
            assertThrows(RuntimeException.class, () -> messageSequencingService.processWithSequence(deal));
            verify(redisService).releaseLock(lockKey, anyString());
        }
    }

    @Nested
    @DisplayName("retryStashedMessages")
    class RetryStashedMessagesTests {
        @Test
        @DisplayName("should retry all stashed messages successfully")
        void shouldRetryAllStashedMessagesSuccessfully() {
            // Given
            String stashKey = "deal:stash:TEST-DEAL-123:1";
            Set<String> stashKeys = Set.of(stashKey);
            when(redisService.keys(anyString())).thenReturn(stashKeys);
            when(messageStasher.retrieve(stashKey)).thenReturn(deal);
            when(redisService.acquireLock(anyString(), anyString(), any(Duration.class))).thenReturn(true);
            when(redisService.releaseLock(anyString(), anyString())).thenReturn(true);

            // When
            messageSequencingService.retryStashedMessages();

            // Then
            verify(messageStasher).retrieve(stashKey);
            verify(sequenceValidator).validate(deal);
        }

        @Test
        @DisplayName("should handle null stashed messages gracefully")
        void shouldHandleNullStashedMessagesGracefully() {
            // Given
            String stashKey = "deal:stash:TEST-DEAL-123:1";
            Set<String> stashKeys = Set.of(stashKey);
            when(redisService.keys(anyString())).thenReturn(stashKeys);
            when(messageStasher.retrieve(stashKey)).thenReturn(null);

            // When
            assertDoesNotThrow(() -> messageSequencingService.retryStashedMessages());

            // Then
            verify(messageStasher).retrieve(stashKey);
            verify(sequenceValidator, never()).validate(any());
        }

        @Test
        @DisplayName("should handle processing failures gracefully")
        void shouldHandleProcessingFailuresGracefully() {
            // Given
            String stashKey = "deal:stash:TEST-DEAL-123:1";
            Set<String> stashKeys = Set.of(stashKey);
            when(redisService.keys(anyString())).thenReturn(stashKeys);
            when(messageStasher.retrieve(stashKey)).thenReturn(deal);
            when(redisService.acquireLock(anyString(), anyString(), any(Duration.class))).thenReturn(false);

            // When
            assertDoesNotThrow(() -> messageSequencingService.retryStashedMessages());

            // Then
            verify(messageStasher).retrieve(stashKey);
            verify(meterRegistry).counter("deal.sequence.lock.acquisition.failed", "dealId", dealId);
        }
    }

    @Nested
    @DisplayName("acquireDistributedLock")
    class AcquireDistributedLockTests {
        @Test
        @DisplayName("should acquire lock successfully on first attempt")
        void shouldAcquireLockSuccessfullyOnFirstAttempt() {
            // Given
            when(redisService.acquireLock(anyString(), anyString(), any(Duration.class))).thenReturn(true);

            // When
            boolean result = messageSequencingService.acquireDistributedLock(lockKey, lockValue);

            // Then
            assertTrue(result);
            verify(redisService).acquireLock(lockKey, lockValue, Duration.ofSeconds(30));
        }

        @Test
        @DisplayName("should retry lock acquisition when first attempt fails")
        void shouldRetryLockAcquisitionWhenFirstAttemptFails() {
            // Given
            when(redisService.acquireLock(anyString(), anyString(), any(Duration.class)))
                .thenReturn(false)
                .thenReturn(true);

            // When
            boolean result = messageSequencingService.acquireDistributedLock(lockKey, lockValue);

            // Then
            assertTrue(result);
            verify(redisService, times(2)).acquireLock(lockKey, lockValue, Duration.ofSeconds(30));
        }

        @Test
        @DisplayName("should return false when lock acquisition times out")
        void shouldReturnFalseWhenLockAcquisitionTimesOut() {
            // Given
            when(redisService.acquireLock(anyString(), anyString(), any(Duration.class))).thenReturn(false);

            // When
            boolean result = messageSequencingService.acquireDistributedLock(lockKey, lockValue);

            // Then
            assertFalse(result);
            verify(redisService, atLeast(2)).acquireLock(lockKey, lockValue, Duration.ofSeconds(30));
        }

        @Test
        @DisplayName("should handle interrupted thread gracefully")
        void shouldHandleInterruptedThreadGracefully() {
            // Given
            when(redisService.acquireLock(anyString(), anyString(), any(Duration.class))).thenReturn(false);
            Thread.currentThread().interrupt();

            // When
            boolean result = messageSequencingService.acquireDistributedLock(lockKey, lockValue);

            // Then
            assertFalse(result);
            assertTrue(Thread.currentThread().isInterrupted());
        }
    }

    @Nested
    @DisplayName("releaseDistributedLock")
    class ReleaseDistributedLockTests {
        @Test
        @DisplayName("should release lock successfully")
        void shouldReleaseLockSuccessfully() {
            // Given
            when(redisService.releaseLock(anyString(), anyString())).thenReturn(true);

            // When
            messageSequencingService.releaseDistributedLock(lockKey, lockValue);

            // Then
            verify(redisService).releaseLock(lockKey, lockValue);
        }

        @Test
        @DisplayName("should handle lock release failure gracefully")
        void shouldHandleLockReleaseFailureGracefully() {
            // Given
            when(redisService.releaseLock(anyString(), anyString())).thenReturn(false);

            // When & Then
            assertDoesNotThrow(() -> messageSequencingService.releaseDistributedLock(lockKey, lockValue));
            verify(redisService).releaseLock(lockKey, lockValue);
        }
    }
} 