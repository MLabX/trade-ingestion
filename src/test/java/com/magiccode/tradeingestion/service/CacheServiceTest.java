package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.model.Counterparty;
import com.magiccode.tradeingestion.model.Instrument;
import com.magiccode.tradeingestion.model.TestDeal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache counterpartyCache;

    @Mock
    private Cache instrumentCache;

    @Mock
    private Cache dealCache;

    @Mock(lenient = true)
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void testCounterpartyCaching() {
        // Setup
        String cacheName = "counterparties";
        String counterpartyId = UUID.randomUUID().toString();
        Counterparty counterparty = new Counterparty();
        counterparty.setId(counterpartyId);
        counterparty.setName("Test Counterparty");

        when(cacheManager.getCache(cacheName)).thenReturn(counterpartyCache);
        when(counterpartyCache.get(counterpartyId, Counterparty.class)).thenReturn(counterparty);

        // Test cache put
        counterpartyCache.put(counterpartyId, counterparty);

        // Test cache get
        Counterparty cachedCounterparty = counterpartyCache.get(counterpartyId, Counterparty.class);
        assertNotNull(cachedCounterparty, "Cached counterparty should not be null");
        assertEquals(counterpartyId, cachedCounterparty.getId(), "Cached counterparty ID should match");
        assertEquals("Test Counterparty", cachedCounterparty.getName(), "Cached counterparty name should match");

        // Test cache evict
        counterpartyCache.evict(counterpartyId);
        verify(counterpartyCache).evict(counterpartyId);
    }

    @Test
    public void testInstrumentCaching() {
        // Setup
        String cacheName = "instruments";
        String instrumentId = UUID.randomUUID().toString();
        Instrument instrument = new Instrument();
        instrument.setId(instrumentId);
        instrument.setSymbol("TEST");

        when(cacheManager.getCache(cacheName)).thenReturn(instrumentCache);
        when(instrumentCache.get(instrumentId, Instrument.class)).thenReturn(instrument);

        // Test cache put
        instrumentCache.put(instrumentId, instrument);

        // Test cache get
        Instrument cachedInstrument = instrumentCache.get(instrumentId, Instrument.class);
        assertNotNull(cachedInstrument, "Cached instrument should not be null");
        assertEquals(instrumentId, cachedInstrument.getId(), "Cached instrument ID should match");
        assertEquals("TEST", cachedInstrument.getSymbol(), "Cached instrument symbol should match");

        // Test cache evict
        instrumentCache.evict(instrumentId);
        verify(instrumentCache).evict(instrumentId);
    }

    @Test
    public void testDealCaching() {
        // Setup
        String cacheName = "deals";
        String dealId = UUID.randomUUID().toString();
        
        TestDeal deal = TestDeal.builder()
                .dealId(dealId)
                .clientId("TEST_CLIENT")
                .instrumentId("TEST_INSTRUMENT")
                .quantity(BigDecimal.ONE)
                .price(BigDecimal.TEN)
                .currency("USD")
                .status("NEW")
                .version(1L)
                .dealDate(LocalDateTime.now())
                .build();

        when(cacheManager.getCache(cacheName)).thenReturn(dealCache);
        when(dealCache.get(dealId, TestDeal.class)).thenReturn(deal);

        // Test cache put
        dealCache.put(dealId, deal);

        // Test cache get
        TestDeal cachedDeal = dealCache.get(dealId, TestDeal.class);
        assertNotNull(cachedDeal, "Cached deal should not be null");
        assertEquals(dealId, cachedDeal.getDealId(), "Cached deal ID should match");
        assertEquals("TEST", cachedDeal.getDealType(), "Cached deal type should match");

        // Test cache evict
        dealCache.evict(dealId);
        verify(dealCache).evict(dealId);
    }
} 