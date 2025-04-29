package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.exception.DealProcessingException;
import com.magiccode.tradeingestion.model.FixedIncomeDerivativeDeal;
import com.magiccode.tradeingestion.repository.FixedIncomeDerivativeDealRepository;
import com.magiccode.tradeingestion.service.transformation.DealTransformationService;
import com.magiccode.tradeingestion.service.validation.DealValidationService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;
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

/**
 * Service class for handling fixed income derivative deals.
 * Provides methods for processing, retrieving, and managing fixed income derivative deals.
 */
@Service
@Slf4j
public class FixedIncomeDerivativeDealService {

    private final FixedIncomeDerivativeDealRepository dealRepository;
    private final JmsTemplate jmsTemplate;
    private final DealValidationService validationService;
    private final DealTransformationService<FixedIncomeDerivativeDeal> transformationService;

    /**
     * Constructs a new FixedIncomeDerivativeDealService.
     *
     * @param dealRepository The repository for fixed income derivative deals
     * @param jmsTemplate The JMS template for message sending
     * @param validationService The service for deal validation
     * @param transformationService The service for deal transformation
     */
    public FixedIncomeDerivativeDealService(
        final FixedIncomeDerivativeDealRepository dealRepository,
        final JmsTemplate jmsTemplate,
        final DealValidationService validationService,
        final DealTransformationService<FixedIncomeDerivativeDeal> transformationService
    ) {
        this.dealRepository = dealRepository;
        this.jmsTemplate = jmsTemplate;
        this.validationService = validationService;
        this.transformationService = transformationService;
    }

    /**
     * Processes a fixed income derivative deal.
     *
     * @param deal The deal to process
     * @return The processed deal
     * @throws DealProcessingException if the deal processing fails
     */
    @Timed(value = "process.fixed.income.deal", 
           description = "Time taken to process a fixed income derivative deal")
    @CircuitBreaker(name = "fixedIncomeDealProcessing", 
                   fallbackMethod = "processDealFallback")
    @RateLimiter(name = "fixedIncomeDealProcessing")
    @Retry(name = "fixedIncomeDealProcessing")
    @Transactional
    public FixedIncomeDerivativeDeal processDeal(final FixedIncomeDerivativeDeal deal) {
        log.info("Processing fixed income derivative deal: {}", deal.getDealId());
        
        try {
            validationService.validateDeal(deal);
        } catch (IllegalArgumentException e) {
            log.error("Validation failed for deal {}: {}", deal.getDealId(), e.getMessage());
            throw new DealProcessingException("Deal validation failed: " + e.getMessage());
        }
        
        Optional<FixedIncomeDerivativeDeal> existingDeal = 
            dealRepository.findByDealId(deal.getDealId());
        
        if (existingDeal.isPresent()) {
            log.error("Deal {} already exists", deal.getDealId());
            throw new DealProcessingException("Deal already exists: " + deal.getDealId());
        }
        
        deal.setProcessedAt(LocalDateTime.now());
        FixedIncomeDerivativeDeal transformedDeal = transformationService.transform(deal);
        FixedIncomeDerivativeDeal savedDeal = dealRepository.save(transformedDeal);
        jmsTemplate.convertAndSend("fixed-income-deals", savedDeal);
        
        log.info("Successfully processed fixed income derivative deal: {}", 
                savedDeal.getDealId());
        
        return savedDeal;
    }

    /**
     * Fallback method for deal processing.
     *
     * @param deal The deal that failed to process
     * @param e The exception that caused the failure
     * @return Never returns, always throws an exception
     * @throws DealProcessingException with details about the failure
     */
    public FixedIncomeDerivativeDeal processDealFallback(
            final FixedIncomeDerivativeDeal deal, 
            final Exception e) {
        log.error("Error processing fixed income derivative deal: {}. Using fallback method.", 
                deal.getDealId(), e);
        throw new DealProcessingException("Failed to process deal: " + e.getMessage(), e);
    }

    /**
     * Retrieves a deal by its ID.
     *
     * @param id The ID of the deal to retrieve
     * @return The found deal
     * @throws DealProcessingException if the deal is not found
     */
    @Timed(value = "get.fixed.income.deal", 
           description = "Time taken to get a fixed income derivative deal by ID")
    @Cacheable(value = "fixedIncomeDeals", key = "#id")
    public FixedIncomeDerivativeDeal getDealById(final UUID id) {
        return dealRepository.findById(id)
            .orElseThrow(() -> new DealProcessingException(
                "Fixed income derivative deal not found with id: " + id));
    }

    /**
     * Retrieves a deal by its deal ID.
     *
     * @param dealId The deal ID to search for
     * @return The found deal
     * @throws DealProcessingException if the deal is not found
     */
    @Timed(value = "get.fixed.income.deal.by.dealid", 
           description = "Time taken to get a fixed income derivative deal by deal ID")
    @Cacheable(value = "fixedIncomeDealsByDealId", key = "#dealId")
    public FixedIncomeDerivativeDeal getDealByDealId(final String dealId) {
        return dealRepository.findByDealId(dealId)
            .orElseThrow(() -> new DealProcessingException(
                "Fixed income derivative deal not found with deal ID: " + dealId));
    }

    /**
     * Retrieves all fixed income derivative deals.
     *
     * @return A list of all deals
     */
    @Timed(value = "get.all.fixed.income.deals", 
           description = "Time taken to get all fixed income derivative deals")
    @Cacheable(value = "allFixedIncomeDeals")
    public List<FixedIncomeDerivativeDeal> getAllDeals() {
        return dealRepository.findAll();
    }

    /**
     * Retrieves deals by their type.
     *
     * @param dealType The type of deals to retrieve
     * @return A list of matching deals
     */
    @Timed(value = "get.fixed.income.deals.by.type", 
           description = "Time taken to get fixed income derivative deals by type")
    @Cacheable(value = "fixedIncomeDealsByType", key = "#dealType")
    public List<FixedIncomeDerivativeDeal> getDealsByType(final String dealType) {
        return dealRepository.findByDealType(dealType);
    }

    /**
     * Retrieves deals by their status.
     *
     * @param status The status to filter by
     * @return A list of matching deals
     */
    @Timed(value = "get.fixed.income.deals.by.status", 
           description = "Time taken to get fixed income derivative deals by status")
    @Cacheable(value = "fixedIncomeDealsByStatus", key = "#status")
    public List<FixedIncomeDerivativeDeal> getDealsByStatus(final String status) {
        return dealRepository.findByStatus(status);
    }

    /**
     * Retrieves deals by counterparty.
     *
     * @param entityId The entity ID of the counterparty
     * @return A list of matching deals
     */
    @Timed(value = "get.fixed.income.deals.by.counterparty", 
           description = "Time taken to get fixed income derivative deals by counterparty")
    @Cacheable(value = "fixedIncomeDealsByCounterparty", key = "#entityId")
    public List<FixedIncomeDerivativeDeal> getDealsByCounterparty(final String entityId) {
        return dealRepository.findByCounterpartyEntityId(entityId);
    }

    /**
     * Clears all caches related to fixed income derivative deals.
     */
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