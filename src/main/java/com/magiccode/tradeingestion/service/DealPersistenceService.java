package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.model.Deal;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ConcurrentModificationException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class DealPersistenceService {
    private final ConcurrentHashMap<String, Lock> dealLocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Deal> dealStore = new ConcurrentHashMap<>();

    @Cacheable(value = "deals", key = "#dealId")
    public Optional<Deal> getDeal(String dealId) {
        return Optional.ofNullable(dealStore.get(dealId));
    }

    @Transactional
    public Deal saveDeal(Deal deal) {
        Lock lock = dealLocks.computeIfAbsent(deal.getDealId(), k -> new ReentrantLock());
        lock.lock();
        try {
            Optional<Deal> existingDeal = getDeal(deal.getDealId());
            if (existingDeal.isPresent() && existingDeal.get().getVersion() >= deal.getVersion()) {
                throw new ConcurrentModificationException(
                    "Deal version conflict: expected version > " + existingDeal.get().getVersion());
            }
            
            Deal savedDeal = deal.withIncrementedVersion();
            dealStore.put(deal.getDealId(), savedDeal);
            return savedDeal;
        } finally {
            lock.unlock();
        }
    }

    @CacheEvict(value = "deals", key = "#dealId")
    public void deleteDeal(String dealId) {
        dealStore.remove(dealId);
        dealLocks.remove(dealId);
    }
} 