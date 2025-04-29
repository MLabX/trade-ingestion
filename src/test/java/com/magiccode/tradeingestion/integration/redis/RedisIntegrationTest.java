package com.magiccode.tradeingestion.integration.redis;

import com.magiccode.tradeingestion.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import static org.junit.jupiter.api.Assertions.*;

class RedisIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testRedisConnection() {
        // Given
        String key = "test:key";
        String value = "test-value";

        // When
        redisTemplate.opsForValue().set(key, value);
        Object retrievedValue = redisTemplate.opsForValue().get(key);

        // Then
        assertEquals(value, retrievedValue);
    }
} 