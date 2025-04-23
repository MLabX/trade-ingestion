package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.exception.DealProcessingException;
import com.magiccode.tradeingestion.model.FixedIncomeDerivativeDeal;
import com.magiccode.tradeingestion.repository.FixedIncomeDerivativeDealRepository;
import com.magiccode.tradeingestion.service.transformation.DealTransformationService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FixedIncomeDerivativeDealService {

    private final FixedIncomeDerivativeDealRepository dealRepository;
    private final JmsTemplate jmsTemplate;
    private final DealValidationService validationService;
    private final DealTransformationService<FixedIncomeDerivativeDeal> transformationService;

    @Timed(value = "process.fixed.income.deal", description = "Time taken to process a fixed income derivative deal")
    @CircuitBreaker(name = "fixedIncomeDealProcessing", fallbackMethod = "processDealFallback")
    @RateLimiter(name = "fixedIncomeDealProcessing")
    @Retry(name = "fixedIncomeDealProcessing")
    @Transactional
    public FixedIncomeDerivativeDeal processDeal(FixedIncomeDerivativeDeal deal) {
        log.info("Processing fixed income derivative deal: {}", deal.getDealId());
        
        // Validate the deal
        validationService.validateDeal(deal);
        
        // Check if deal already exists
        Optional<FixedIncomeDerivativeDeal> existingDeal = dealRepository.findByDealId(deal.getDealId());
        
        if (existingDeal.isPresent()) {
            // Version check for optimistic locking
            if (existingDeal.get().getVersion() > deal.getVersion()) {
                log.warn("Deal {} has been updated by another process. Current version: {}, Attempted version: {}", 
                    deal.getDealId(), existingDeal.get().getVersion(), deal.getVersion());
                throw new DealProcessingException("Deal has been updated by another process");
            }
        }
        
        // Set processing timestamp
        deal.setProcessedAt(LocalDateTime.now());
        
        // Transform the deal if needed
        FixedIncomeDerivativeDeal transformedDeal = transformationService.transform(deal);
        
        // Save the deal
        FixedIncomeDerivativeDeal savedDeal = dealRepository.save(transformedDeal);
        
        // Send to queue for downstream processing
        jmsTemplate.convertAndSend("fixed-income-deals", savedDeal);
        
        log.info("Successfully processed fixed income derivative deal: {}", savedDeal.getDealId());
        
        return savedDeal;
    }

    public FixedIncomeDerivativeDeal processDealFallback(FixedIncomeDerivativeDeal deal, Exception e) {
        log.error("Error processing fixed income derivative deal: {}. Using fallback method.", deal.getDealId(), e);
        throw new DealProcessingException("Failed to process deal: " + e.getMessage(), e);
    }

    @Timed(value = "get.fixed.income.deal", description = "Time taken to get a fixed income derivative deal by ID")
    @Cacheable(value = "fixedIncomeDeals", key = "#id")
    public FixedIncomeDerivativeDeal getDealById(UUID id) {
        return dealRepository.findById(id)
            .orElseThrow(() -> new DealProcessingException("Fixed income derivative deal not found with id: " + id));
    }

    @Timed(value = "get.fixed.income.deal.by.dealid", description = "Time taken to get a fixed income derivative deal by deal ID")
    @Cacheable(value = "fixedIncomeDealsByDealId", key = "#dealId")
    public FixedIncomeDerivativeDeal getDealByDealId(String dealId) {
        return dealRepository.findByDealId(dealId)
            .orElseThrow(() -> new DealProcessingException("Fixed income derivative deal not found with deal ID: " + dealId));
    }

    @Timed(value = "get.all.fixed.income.deals", description = "Time taken to get all fixed income derivative deals")
    @Cacheable(value = "allFixedIncomeDeals")
    public List<FixedIncomeDerivativeDeal> getAllDeals() {
        return dealRepository.findAll();
    }

    @Timed(value = "get.fixed.income.deals.by.type", description = "Time taken to get fixed income derivative deals by type")
    @Cacheable(value = "fixedIncomeDealsByType", key = "#dealType")
    public List<FixedIncomeDerivativeDeal> getDealsByType(String dealType) {
        return dealRepository.findByDealType(dealType);
    }

    @Timed(value = "get.fixed.income.deals.by.status", description = "Time taken to get fixed income derivative deals by status")
    @Cacheable(value = "fixedIncomeDealsByStatus", key = "#status")
    public List<FixedIncomeDerivativeDeal> getDealsByStatus(String status) {
        return dealRepository.findByStatus(status);
    }

    @Timed(value = "get.fixed.income.deals.by.counterparty", description = "Time taken to get fixed income derivative deals by counterparty")
    @Cacheable(value = "fixedIncomeDealsByCounterparty", key = "#entityId")
    public List<FixedIncomeDerivativeDeal> getDealsByCounterparty(String entityId) {
        return dealRepository.findByCounterpartyEntityId(entityId);
    }

    @CacheEvict(value = {
        "fixedIncomeDeals",
        "fixedIncomeDealsByDealId",
        "allFixedIncomeDeals",
        "fixedIncomeDealsByType",
        "fixedIncomeDealsByStatus",
        "fixedIncomeDealsByCounterparty"
    }, allEntries = true)
    public void clearCache() {
        log.info("Cache cleared for fixed income derivative deals");
    }
} 