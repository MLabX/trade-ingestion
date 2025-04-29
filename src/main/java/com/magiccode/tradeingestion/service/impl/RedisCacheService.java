package com.magiccode.tradeingestion.service.impl;

import com.magiccode.tradeingestion.model.Counterparty;
import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.model.Instrument;
import com.magiccode.tradeingestion.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis-based implementation of the CacheService interface.
 * 
 * This service provides caching functionality using Redis as the underlying
 * storage mechanism. It supports caching of deals, counterparties, and instruments
 * with configurable TTL (Time To Live) values.
 * 
 * Key features:
 * - Configurable TTL for cached items
 * - Type-safe caching operations
 * - Automatic cache eviction
 * - Support for multiple entity types
 * 
 * @see CacheService
 * @see Deal
 * @see Counterparty
 * @see Instrument
 */
@Service
public class RedisCacheService implements CacheService {

    private static final String DEAL_CACHE_PREFIX = "deal:";
    private static final String COUNTERPARTY_CACHE_PREFIX = "counterparty:";
    private static final String INSTRUMENT_CACHE_PREFIX = "instrument:";
    private static final long DEFAULT_TTL_HOURS = 24;

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RedisCacheService(final RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void cacheDeal(final Deal deal) {
        String key = DEAL_CACHE_PREFIX + deal.getDealId();
        redisTemplate.opsForValue().set(key, deal, DEFAULT_TTL_HOURS, TimeUnit.HOURS);
    }

    @Override
    public Deal getDeal(final String dealId) {
        String key = DEAL_CACHE_PREFIX + dealId;
        return (Deal) redisTemplate.opsForValue().get(key);
    }

    @Override
    public void evictDeal(final String dealId) {
        String key = DEAL_CACHE_PREFIX + dealId;
        redisTemplate.delete(key);
    }

    @Override
    public void cacheCounterparty(final Counterparty counterparty) {
        String key = COUNTERPARTY_CACHE_PREFIX + counterparty.getId();
        redisTemplate.opsForValue().set(key, counterparty, DEFAULT_TTL_HOURS, TimeUnit.HOURS);
    }

    @Override
    public Counterparty getCounterparty(final String counterpartyId) {
        String key = COUNTERPARTY_CACHE_PREFIX + counterpartyId;
        return (Counterparty) redisTemplate.opsForValue().get(key);
    }

    @Override
    public void evictCounterparty(final String counterpartyId) {
        String key = COUNTERPARTY_CACHE_PREFIX + counterpartyId;
        redisTemplate.delete(key);
    }

    @Override
    public void cacheInstrument(final Instrument instrument) {
        String key = INSTRUMENT_CACHE_PREFIX + instrument.getId();
        redisTemplate.opsForValue().set(key, instrument, DEFAULT_TTL_HOURS, TimeUnit.HOURS);
    }

    @Override
    public Instrument getInstrument(final String instrumentId) {
        String key = INSTRUMENT_CACHE_PREFIX + instrumentId;
        return (Instrument) redisTemplate.opsForValue().get(key);
    }

    @Override
    public void evictInstrument(final String instrumentId) {
        String key = INSTRUMENT_CACHE_PREFIX + instrumentId;
        redisTemplate.delete(key);
    }
} 