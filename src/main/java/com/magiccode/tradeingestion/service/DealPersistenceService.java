package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.exception.DealProcessingException;
import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
@Slf4j
public class DealPersistenceService {
    private final ConcurrentHashMap<String, Lock> dealLocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Deal> dealStore = new ConcurrentHashMap<>();
    private final DealRepository dealRepository;

    @Cacheable(value = "deals", key = "#dealId")
    public Optional<Deal> getDeal(String dealId) {
        return Optional.ofNullable(dealStore.get(dealId));
    }

    @Transactional
    public Deal saveDeal(Deal deal) {
        log.info("Saving deal: {}", deal.getDealId());
        
        // Check if deal already exists
        Optional<Deal> existingDeal = dealRepository.findByDealId(deal.getDealId());
        
        if (existingDeal.isPresent()) {
            // Version check for optimistic locking
            if (existingDeal.get().getVersion() > deal.getVersion()) {
                log.warn("Deal {} has been updated by another process. Current version: {}, Attempted version: {}", 
                    deal.getDealId(), existingDeal.get().getVersion(), deal.getVersion());
                throw new DealProcessingException("Deal has been updated by another process");
            }
        }
        
        // Save the deal
        Deal savedDeal = dealRepository.save(deal);
        
        log.info("Successfully saved deal: {}", savedDeal.getDealId());
        
        return savedDeal;
    }

    @CacheEvict(value = "deals", key = "#dealId")
    public void deleteDeal(String dealId) {
        dealStore.remove(dealId);
        dealLocks.remove(dealId);
    }
} 