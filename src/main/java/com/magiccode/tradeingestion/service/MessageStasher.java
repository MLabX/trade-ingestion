package com.magiccode.tradeingestion.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magiccode.tradeingestion.exception.DealProcessingException;
import com.magiccode.tradeingestion.model.Deal;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

/**
 * Component responsible for temporarily storing and retrieving out-of-order messages
 * in a distributed system using Redis as a backing store.
 * 
 * This component provides functionality to:
 * 1. Stash messages that arrive out of sequence for later processing
 * 2. Retrieve stashed messages when they're ready to be processed
 * 3. Clean up stashed messages after successful processing
 * 
 * Messages are stored with a configurable TTL to prevent resource exhaustion
 * and automatically expire if not processed within the time window.
 * 
 * @see Deal
 * @see RedisService
 * @see MessageSequencingService
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MessageStasher {
    private static final String STASH_KEY_PREFIX = "deal:stash:";
    private static final Duration STASH_TTL = Duration.ofHours(1);

    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    public void stash(Deal deal) {
        try {
            String stashKey = STASH_KEY_PREFIX + deal.getDealId() + ":" + deal.getVersion();
            String dealJson = objectMapper.writeValueAsString(deal);
            redisService.set(stashKey, dealJson, STASH_TTL);
            log.info("Stashed message for deal {} with version {}", deal.getDealId(), deal.getVersion());
            meterRegistry.counter("deal.sequence.message.stashed", "dealId", deal.getDealId()).increment();
        } catch (JsonProcessingException e) {
            log.error("Failed to stash message for deal {}: {}", deal.getDealId(), e.getMessage());
            throw new DealProcessingException("Failed to stash message", e);
        }
    }

    public void cleanup(String dealId) {
        Set<String> stashKeys = redisService.keys(STASH_KEY_PREFIX + dealId + "*");
        if (stashKeys != null) {
            redisService.delete(stashKeys);
            log.info("Cleaned up stashed messages for deal {}", dealId);
        }
    }

    public Deal retrieve(String stashKey) {
        try {
            String dealJson = redisService.get(stashKey);
            if (dealJson != null) {
                return objectMapper.readValue(dealJson, Deal.class);
            }
            return null;
        } catch (JsonProcessingException e) {
            log.error("Failed to retrieve stashed message with key {}: {}", stashKey, e.getMessage());
            return null;
        }
    }
} 
