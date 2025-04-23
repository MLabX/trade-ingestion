package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.exception.DealProcessingException;
import com.magiccode.tradeingestion.model.Deal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageSequencingService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ConcurrentHashMap<String, Lock> dealLocks = new ConcurrentHashMap<>();
    private static final String SEQUENCE_KEY_PREFIX = "deal:sequence:";
    private static final String STASH_KEY_PREFIX = "deal:stash:";
    private static final Duration SEQUENCE_TTL = Duration.ofHours(24);
    private static final Duration STASH_TTL = Duration.ofHours(1);

    public void validateMessageSequence(Deal deal) {
        String dealId = deal.getDealId();
        String eventType = deal.getEventType();
        long version = deal.getVersion();

        // Get the last processed version for this deal
        String lastVersionStr = redisTemplate.opsForValue().get(SEQUENCE_KEY_PREFIX + dealId);
        long lastVersion = lastVersionStr != null ? Long.parseLong(lastVersionStr) : 0;

        // Validate version sequence
        if (version <= lastVersion) {
            log.warn("Duplicate or outdated message received for deal {}: version {} <= last version {}",
                dealId, version, lastVersion);
            throw new DealProcessingException("Duplicate or outdated message");
        }

        // Validate event type sequence
        if (!isValidEventSequence(dealId, eventType)) {
            log.warn("Invalid event sequence for deal {}: event type {} is not allowed at this point",
                dealId, eventType);
            stashMessage(deal);
            throw new DealProcessingException("Invalid event sequence");
        }

        // Update the last processed version
        redisTemplate.opsForValue().set(SEQUENCE_KEY_PREFIX + dealId, String.valueOf(version), SEQUENCE_TTL);
    }

    private boolean isValidEventSequence(String dealId, String eventType) {
        String lastEventType = redisTemplate.opsForValue().get(SEQUENCE_KEY_PREFIX + dealId + ":event");
        
        // If no previous event, only CREATE is allowed
        if (lastEventType == null) {
            return "CREATE".equals(eventType);
        }

        // Validate event sequence
        return switch (lastEventType) {
            case "CREATE" -> "MODIFY".equals(eventType) || "CANCEL".equals(eventType);
            case "MODIFY" -> "MODIFY".equals(eventType) || "CANCEL".equals(eventType) || "SETTLEMENT".equals(eventType);
            case "CANCEL" -> false; // No events allowed after CANCEL
            case "SETTLEMENT" -> false; // No events allowed after SETTLEMENT
            default -> false;
        };
    }

    private void stashMessage(Deal deal) {
        String stashKey = STASH_KEY_PREFIX + deal.getDealId() + ":" + deal.getVersion();
        redisTemplate.opsForValue().set(stashKey, deal.toString(), STASH_TTL);
        log.info("Stashed message for deal {} with version {}", deal.getDealId(), deal.getVersion());
    }

    public Lock getDealLock(String dealId) {
        return dealLocks.computeIfAbsent(dealId, k -> new ReentrantLock());
    }

    public void cleanupStashedMessages(String dealId) {
        String pattern = STASH_KEY_PREFIX + dealId + ":*";
        redisTemplate.delete(redisTemplate.keys(pattern));
        log.info("Cleaned up stashed messages for deal {}", dealId);
    }
} 