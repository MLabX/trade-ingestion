package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.exception.DealProcessingException;
import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.repository.DealRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Service responsible for ingesting and processing deals in a resilient and consistent manner.
 * 
 * Key features:
 * - Ensures proper message sequencing using distributed locks
 * - Validates and transforms deals before persistence
 * - Implements circuit breaker pattern for fault tolerance
 * - Provides caching for frequently accessed deals
 * - Supports retry mechanism for transient failures
 * - Maintains transaction boundaries
 * 
 * The service follows a pipeline pattern:
 * 1. Message sequencing check
 * 2. Deal validation
 * 3. Deal transformation
 * 4. Persistent storage with resilience patterns
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DealIngestionService {

    // Core dependencies for deal processing
    private final DealRepository dealRepository;
    private final DealValidationService dealValidationService;
    private final DealTransformationService dealTransformationService;
    private final MessageSequencingService messageSequencingService;

    // Resilience4j components for fault tolerance
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    /**
     * Processes a deal through the complete pipeline with resilience patterns.
     * 
     * The processing pipeline includes:
     * 1. Message sequencing validation to ensure ordered processing
     * 2. Business rule validation
     * 3. Data transformation
     * 4. Persistent storage with circuit breaker and retry mechanisms
     * 
     * @param deal The deal to process
     * @return The processed and persisted deal
     * @throws DealProcessingException if any step in the pipeline fails
     */
    @Transactional
    @Timed(value = "process.deal", description = "Time taken to process a deal")
    public Deal processDeal(Deal deal) {
        log.info("Processing deal: {}", deal.getDealId());

        // Step 1: Ensure proper message sequencing
        messageSequencingService.processWithSequence(deal);

        // Step 2: Validate deal against business rules
        dealValidationService.validateDealOrThrow(deal);

        // Step 3: Transform deal data
        Deal transformedDeal = dealTransformationService.transformDeal(deal);

        // Step 4: Save deal with resilience patterns
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("dealProcessing");
        Retry retry = retryRegistry.retry("dealProcessing");

        // Combine circuit breaker and retry patterns for robust persistence
        Supplier<Deal> decoratedSupplier = CircuitBreaker
            .decorateSupplier(circuitBreaker, () -> {
                try {
                    return retry.executeSupplier(() -> {
                        log.info("Saving deal to database: {}", transformedDeal.getDealId());
                        return dealRepository.save(transformedDeal);
                    });
                } catch (Exception e) {
                    log.error("Error saving deal: {}", e.getMessage(), e);
                    throw new DealProcessingException("Failed to save deal", e);
                }
            });

        return decoratedSupplier.get();
    }

    /**
     * Retrieves a deal by its unique identifier.
     * Results are cached to improve performance for frequently accessed deals.
     * 
     * @param id The UUID of the deal
     * @return Optional containing the deal if found, empty otherwise
     */
    @Cacheable(value = "deals", key = "#id")
    public Optional<Deal> getDealById(UUID id) {
        log.info("Retrieving deal by ID: {}", id);
        return dealRepository.findById(id);
    }

    /**
     * Retrieves all deals in the system.
     * Results are cached to improve performance.
     * Use with caution on large datasets.
     * 
     * @return List of all deals
     */
    @Cacheable(value = "deals", key = "'all'")
    public List<Deal> getAllDeals() {
        log.info("Retrieving all deals");
        return dealRepository.findAll();
    }

    /**
     * Retrieves deals by their instrument symbol.
     * Results are cached per symbol to improve performance.
     * 
     * @param symbol The instrument symbol to search for
     * @return List of deals for the given symbol
     */
    @Cacheable(value = "deals", key = "#symbol")
    public List<Deal> getDealsBySymbol(String symbol) {
        log.info("Retrieving deals by symbol: {}", symbol);
        return dealRepository.findByInstrumentId(symbol);
    }

    /**
     * Clears the deal cache.
     * Should be called when deal data is modified outside this service.
     */
    @CacheEvict(value = "deals", allEntries = true)
    public void clearCache() {
        log.info("Clearing deals cache");
    }
} 
