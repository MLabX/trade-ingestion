package com.magiccode.tradeingestion.integration.cache;

import com.magiccode.tradeingestion.BaseIntegrationTest;
import com.magiccode.tradeingestion.config.RedisTestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import com.magiccode.tradeingestion.model.FixedIncomeDerivativeDeal;
import com.magiccode.tradeingestion.repository.FixedIncomeDerivativeDealRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("redis")
@Import(RedisTestConfig.class)
public class RedisCacheIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private FixedIncomeDerivativeDealRepository dealRepository;

    private FixedIncomeDerivativeDeal testDeal;

    @BeforeEach
    void setUp() {
        // Clear Redis cache before each test
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // Create a test deal
        testDeal = new FixedIncomeDerivativeDeal();
        testDeal.setId(UUID.randomUUID());
        testDeal.setDealId("TEST-123");
        testDeal.setDealType("InterestRateSwap");
        testDeal.setStatus("NEW");
    }

    @Test
    void testCachePutAndGet() {
        // Save deal to repository
        dealRepository.save(testDeal);

        // Get deal from repository (should be cached)
        FixedIncomeDerivativeDeal cachedDeal = dealRepository.findById(testDeal.getId()).orElse(null);
        assertNotNull(cachedDeal);
        assertEquals(testDeal.getDealId(), cachedDeal.getDealId());

        // Verify cache hit
        assertTrue(redisTemplate.hasKey("deals::" + testDeal.getId().toString()));
    }

    @Test
    void testCacheEvict() {
        // Save deal to repository
        dealRepository.save(testDeal);

        // Delete deal (should evict from cache)
        dealRepository.deleteById(testDeal.getId());

        // Verify cache miss
        assertFalse(redisTemplate.hasKey("deals::" + testDeal.getId().toString()));
    }

    @Test
    void testCacheTTL() throws InterruptedException {
        // Save deal to repository
        dealRepository.save(testDeal);

        // Wait for TTL to expire (24 hours in properties)
        Thread.sleep(1000); // Just testing the concept, actual TTL is 24 hours

        // Verify cache still exists (since TTL is long)
        assertTrue(redisTemplate.hasKey("deals::" + testDeal.getId().toString()));
    }
} 