package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.model.Counterparty;
import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.model.Instrument;

/**
 * Service interface for caching financial entities.
 * Provides methods for caching and retrieving deals, counterparties, and instruments.
 */
public interface CacheService {

    /**
     * Caches a deal with a default TTL.
     *
     * @param deal The deal to cache
     */
    void cacheDeal(Deal deal);

    /**
     * Retrieves a deal from the cache.
     *
     * @param dealId The ID of the deal to retrieve
     * @return The cached deal, or null if not found
     */
    Deal getDeal(String dealId);

    /**
     * Evicts a deal from the cache.
     *
     * @param dealId The ID of the deal to evict
     */
    void evictDeal(String dealId);

    /**
     * Caches a counterparty with a default TTL.
     *
     * @param counterparty The counterparty to cache
     */
    void cacheCounterparty(Counterparty counterparty);

    /**
     * Retrieves a counterparty from the cache.
     *
     * @param counterpartyId The ID of the counterparty to retrieve
     * @return The cached counterparty, or null if not found
     */
    Counterparty getCounterparty(String counterpartyId);

    /**
     * Evicts a counterparty from the cache.
     *
     * @param counterpartyId The ID of the counterparty to evict
     */
    void evictCounterparty(String counterpartyId);

    /**
     * Caches an instrument with a default TTL.
     *
     * @param instrument The instrument to cache
     */
    void cacheInstrument(Instrument instrument);

    /**
     * Retrieves an instrument from the cache.
     *
     * @param instrumentId The ID of the instrument to retrieve
     * @return The cached instrument, or null if not found
     */
    Instrument getInstrument(String instrumentId);

    /**
     * Evicts an instrument from the cache.
     *
     * @param instrumentId The ID of the instrument to evict
     */
    void evictInstrument(String instrumentId);
} 