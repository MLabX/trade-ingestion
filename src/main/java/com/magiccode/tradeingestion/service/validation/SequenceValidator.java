package com.magiccode.tradeingestion.service.validation;

import com.magiccode.tradeingestion.exception.DealProcessingException;
import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.service.RedisService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class SequenceValidator {
    private static final String SEQUENCE_KEY_PREFIX = "deal:sequence:";
    private static final Duration SEQUENCE_TTL = Duration.ofHours(24);

    private final RedisService redisService;
    private final MeterRegistry meterRegistry;
    private final EventTypeValidator eventTypeValidator;

    public void validate(Deal deal) {
        String dealId = deal.getDealId();
        String eventType = deal.getEventType();
        long version = deal.getVersion();

        // Get the last processed version for this deal
        String lastVersionStr = redisService.get(SEQUENCE_KEY_PREFIX + dealId);
        long lastVersion = lastVersionStr != null ? Long.parseLong(lastVersionStr) : 0;

        // Validate version sequence
        if (version <= lastVersion) {
            log.warn("Duplicate or outdated message received for deal {}: version {} <= last version {}",
                dealId, version, lastVersion);
            meterRegistry.counter("deal.sequence.validation.failed", "reason", "duplicate", "dealId", dealId).increment();
            throw new DealProcessingException("Duplicate or outdated message");
        }

        // Validate event type sequence
        if (!eventTypeValidator.isValid(dealId, eventType)) {
            log.warn("Invalid event sequence for deal {}: event type {} is not allowed at this point",
                dealId, eventType);
            meterRegistry.counter("deal.sequence.validation.failed", "reason", "invalid_sequence", "dealId", dealId).increment();
            throw new DealProcessingException("Invalid event sequence");
        }

        // Update the last processed version
        redisService.set(SEQUENCE_KEY_PREFIX + dealId, String.valueOf(version), SEQUENCE_TTL);
    }
} 