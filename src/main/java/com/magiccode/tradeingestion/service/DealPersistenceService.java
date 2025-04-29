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

/**
 * Service responsible for persisting and retrieving deal data.
 * 
 * This service provides functionality to:
 * 1. Save and update deals in the database
 * 2. Retrieve deals by various criteria
 * 3. Handle concurrent modifications
 * 4. Manage deal versioning
 * 
 * The service uses optimistic locking to handle concurrent modifications
 * and provides caching capabilities for improved performance.
 * 
 * @see Deal
 * @see DealRepository
 * @see DealProcessingException
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DealPersistenceService {
    private final ConcurrentHashMap<String, Lock> dealLocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Deal> dealStore = new ConcurrentHashMap<>();
    private final DealRepository dealRepository;

    /**
     * Retrieves a deal by its ID.
     *
     * @param id The ID of the deal to retrieve
     * @return The found deal
     * @throws DealProcessingException if the deal is not found
     */
    @Cacheable(value = "deals", key = "#id")
    public Deal getDealById(final UUID id) {
        return dealRepository.findById(id)
            .orElseThrow(() -> new DealProcessingException(
                "Deal not found with id: " + id));
    }

    /**
     * Retrieves a deal by its deal ID.
     *
     * @param dealId The deal ID to search for
     * @return The found deal
     * @throws DealProcessingException if the deal is not found
     */
    @Cacheable(value = "dealsByDealId", key = "#dealId")
    public Deal getDealByDealId(final String dealId) {
        return dealRepository.findByDealId(dealId)
            .orElseThrow(() -> new DealProcessingException(
                "Deal not found with deal ID: " + dealId));
    }

    /**
     * Saves a deal to the database with optimistic locking.
     *
     * @param deal The deal to save
     * @return The saved deal
     * @throws DealProcessingException if the deal cannot be saved
     */
    @Transactional
    public Deal saveDeal(final Deal deal) {
        final String dealId = deal.getDealId();
        final ReentrantLock lock = (ReentrantLock) dealLocks.computeIfAbsent(dealId, k -> new ReentrantLock());

        try {
            if (lock.tryLock()) {
                try {
                    return saveDealWithLock(deal);
                } finally {
                    lock.unlock();
                    // Clean up the lock if no other thread is waiting
                    if (!lock.hasQueuedThreads()) {
                        dealLocks.remove(dealId);
                    }
                }
            } else {
                log.warn("Could not acquire lock for deal {} - another thread is currently processing this deal", dealId);
                throw new DealProcessingException("Could not acquire lock for deal: " + dealId + " - another thread is currently processing this deal");
            }
        } catch (ConcurrentModificationException e) {
            log.warn("Concurrent modification detected for deal {}: {}", dealId, e.getMessage());
            throw e; // Rethrow the original ConcurrentModificationException
        } catch (Exception e) {
            log.error("Error saving deal {}: {}", dealId, e.getMessage(), e);
            throw new DealProcessingException("Error saving deal: " + e.getMessage(), e);
        }
    }

    private Deal saveDealWithLock(final Deal deal) {
        final String dealId = deal.getDealId();
        final Optional<Deal> existingDeal = dealRepository.findByDealId(dealId);

        if (existingDeal.isPresent()) {
            final Deal currentDeal = existingDeal.get();
            if (currentDeal.getVersion() != deal.getVersion()) {
                log.warn("Version mismatch for deal {}: current version {}, incoming version {}", 
                    dealId, currentDeal.getVersion(), deal.getVersion());
                throw new ConcurrentModificationException(
                    "Deal " + dealId + " has been modified by another process");
            }
            deal.setVersion(deal.getVersion() + 1);
            log.debug("Incremented version for deal {} to {}", dealId, deal.getVersion());
        }

        return dealRepository.save(deal);
    }

    /**
     * Clears the deal cache.
     */
    @CacheEvict(value = {"deals", "dealsByDealId"}, allEntries = true)
    public void clearCache() {
        log.info("Cache cleared for deals");
    }

    @CacheEvict(value = "deals", key = "#dealId")
    public void deleteDeal(String dealId) {
        dealStore.remove(dealId);
        dealLocks.remove(dealId);
    }

    /**
     * Cleans up any stale locks that have been abandoned.
     * This method should be called periodically to prevent memory leaks.
     */
    public void cleanupStaleLocks() {
        dealLocks.entrySet().removeIf(entry -> {
            ReentrantLock lock = (ReentrantLock) entry.getValue();
            return !lock.hasQueuedThreads() && !lock.isLocked();
        });
    }
} 
