package com.magiccode.tradeingestion.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class RedisConfigTest {

    @Autowired
    @Qualifier("testRedisTemplate")
    private RedisTemplate<String, String> testRedisTemplate;

    @Autowired
    @Qualifier("testCacheManager")
    private CacheManager testCacheManager;

    @Test
    public void testRedisTemplateConfiguration() {
        assertNotNull(testRedisTemplate, "Test RedisTemplate should not be null");
        assertNotNull(testRedisTemplate.getConnectionFactory(), "Redis connection factory should not be null");
    }

    @Test
    public void testCacheManagerConfiguration() {
        assertNotNull(testCacheManager, "Test CacheManager should not be null");
        assertNotNull(testCacheManager.getCache("counterparties"), "Counterparties cache should exist");
        assertNotNull(testCacheManager.getCache("instruments"), "Instruments cache should exist");
        assertNotNull(testCacheManager.getCache("deals"), "Deals cache should exist");
    }

    @Test
    public void testRedisOperations() {
        String key = "testKey";
        String value = "testValue";

        // Test set operation
        testRedisTemplate.opsForValue().set(key, value);
        
        // Test get operation
        String retrievedValue = testRedisTemplate.opsForValue().get(key);
        assertEquals(value, retrievedValue, "Retrieved value should match set value");

        // Test delete operation
        testRedisTemplate.delete(key);
        String deletedValue = testRedisTemplate.opsForValue().get(key);
        assertNull(deletedValue, "Value should be null after deletion");
    }

    @Test
    public void testCacheOperations() {
        String cacheName = "testCache";
        String key = "testKey";
        String value = "testValue";

        // Test cache put
        testCacheManager.getCache(cacheName).put(key, value);

        // Test cache get
        String cachedValue = testCacheManager.getCache(cacheName).get(key, String.class);
        assertEquals(value, cachedValue, "Cached value should match put value");

        // Test cache evict
        testCacheManager.getCache(cacheName).evict(key);
        String evictedValue = testCacheManager.getCache(cacheName).get(key, String.class);
        assertNull(evictedValue, "Value should be null after eviction");
    }
} 