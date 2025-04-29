package com.magiccode.tradeingestion.service.validation;

import com.magiccode.tradeingestion.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class EventTypeValidator {
    private static final String SEQUENCE_KEY_PREFIX = "deal:sequence:";
    private static final Duration SEQUENCE_TTL = Duration.ofHours(24);

    private final RedisService redisService;

    public boolean isValid(String dealId, String eventType) {
        String lastEventType = redisService.get(SEQUENCE_KEY_PREFIX + dealId + ":event");

        // If no previous event, only CREATED is allowed
        if (lastEventType == null) {
            return "CREATED".equals(eventType);
        }

        // Validate event sequence based on deal lifecycle
        return switch (lastEventType) {
            case "CREATED" -> "UPDATED".equals(eventType) || "CANCELLED".equals(eventType);
            case "UPDATED" -> "UPDATED".equals(eventType) || "CANCELLED".equals(eventType);
            case "CANCELLED" -> false; // No events allowed after CANCELLED
            default -> false;
        };
    }

    public void updateLastEventType(String dealId, String eventType) {
        redisService.set(SEQUENCE_KEY_PREFIX + dealId + ":event", eventType, SEQUENCE_TTL);
    }
} 