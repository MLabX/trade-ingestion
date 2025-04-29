package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.exception.DealProcessingException;
import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.service.validation.SequenceValidator;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Service responsible for ensuring message sequencing and preventing duplicate processing
 * in a distributed environment with multiple instances.
 * 
 * This service provides functionality to:
 * 1. Ensure messages are processed in the correct sequence
 * 2. Prevent duplicate processing of messages
 * 3. Handle concurrent message processing across multiple instances
 * 4. Manage message stashing for out-of-order messages with automatic retry
 * 5. Prevent race conditions using distributed locks
 * 
 * The service uses Redis for distributed locking and sequence tracking,
 * with configurable TTLs for both sequence and stash entries.
 * 
 * @see Deal
 * @see DealProcessingException
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageSequencingService {
    private static final String LOCK_KEY_PREFIX = "deal:lock:";
    private static final String STASH_KEY_PREFIX = "deal:stash:";
    private static final Duration LOCK_TTL = Duration.ofSeconds(30);
    private static final long LOCK_WAIT_TIME = 5000; // 5 seconds
    private static final long LOCK_RETRY_INTERVAL = 100; // 100 milliseconds

    private final RedisService redisService;
    private final MeterRegistry meterRegistry;
    private final SequenceValidator sequenceValidator;
    private final MessageStasher messageStasher;
    private final ConcurrentHashMap<String, Lock> dealLocks = new ConcurrentHashMap<>();

    /**
     * Gets or creates a lock for a specific deal.
     * This method provides thread-safe access to deal-specific locks.
     * 
     * @param dealId The deal ID to get the lock for
     * @return The lock instance for the deal
     */
    public Lock getDealLock(String dealId) {
        return dealLocks.computeIfAbsent(dealId, k -> new ReentrantLock());
    }

    /**
     * Processes a deal with sequence checking to ensure proper ordering.
     * @param deal The deal to process
     * @throws DealProcessingException if the deal cannot be processed
     */
    public void processWithSequence(Deal deal) {
        String dealId = deal.getDealId();
        String lockKey = LOCK_KEY_PREFIX + dealId;
        String lockValue = Thread.currentThread().getName() + ":" + System.currentTimeMillis();

        try {
            // Try to acquire distributed lock
            if (!acquireDistributedLock(lockKey, lockValue)) {
                log.warn("Could not acquire distributed lock for deal {}", dealId);
                meterRegistry.counter("deal.sequence.lock.acquisition.failed", "dealId", dealId).increment();
                throw new DealProcessingException("Could not acquire lock for deal: " + dealId);
            }

            try {
                // Check sequence and validate event order
                sequenceValidator.validate(deal);

                // Process the deal
                processDealWithSequence(deal);

                meterRegistry.counter("deal.sequence.processed.success", "dealId", dealId).increment();
            } finally {
                // Always release the lock
                releaseDistributedLock(lockKey, lockValue);
            }
        } catch (DealProcessingException e) {
            meterRegistry.counter("deal.sequence.processed.failed", "dealId", dealId).increment();
            throw e;
        }
    }

    boolean acquireDistributedLock(String lockKey, String lockValue) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < LOCK_WAIT_TIME) {
            if (redisService.acquireLock(lockKey, lockValue, LOCK_TTL)) {
                return true;
            }

            try {
                Thread.sleep(LOCK_RETRY_INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    void releaseDistributedLock(String lockKey, String lockValue) {
        redisService.releaseLock(lockKey, lockValue);
    }

    private void processDealWithSequence(Deal deal) {
        String dealId = deal.getDealId();
        String sequenceKey = "deal:sequence:" + dealId;

        try {
            // Always try to increment the sequence first
            Long currentSequence = redisService.increment(sequenceKey);

            if (currentSequence == null) {
                // If increment failed, try to set the initial sequence
                currentSequence = 1L;
                Boolean setResult = redisService.setIfAbsent(sequenceKey, currentSequence.toString(), Duration.ofHours(24));

                if (setResult == null || !setResult) {
                    log.warn("Failed to increment sequence for deal {}: could not set initial sequence", dealId);
                    throw new DealProcessingException("Failed to increment sequence for deal: " + dealId);
                }
            } else {
                redisService.expire(sequenceKey, Duration.ofHours(24));
            }

            log.debug("Processing deal {} with sequence {}", dealId, currentSequence);

            // Process the deal here
            // This is where the actual deal processing logic would go
            // For now, we just log the processing
            log.info("Processing deal {} with event type {}", dealId, deal.getEventType());

            // Clean up any stashed messages for this deal
            messageStasher.cleanup(dealId);
        } catch (Exception e) {
            log.error("Error processing deal {}: {}", dealId, e.getMessage());
            throw new DealProcessingException("Failed to process deal: " + dealId, e);
        }
    }

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void retryStashedMessages() {
        log.info("Starting retry of stashed messages");
        Set<String> stashKeys = redisService.keys(STASH_KEY_PREFIX + "*");

        if (stashKeys != null) {
            for (String stashKey : stashKeys) {
                try {
                    Deal deal = messageStasher.retrieve(stashKey);
                    if (deal != null) {
                        processWithSequence(deal);
                        log.info("Successfully retried stashed message for deal {}", deal.getDealId());
                    }
                } catch (Exception e) {
                    log.error("Failed to retry stashed message with key {}: {}", stashKey, e.getMessage());
                }
            }
        }
    }
} 
