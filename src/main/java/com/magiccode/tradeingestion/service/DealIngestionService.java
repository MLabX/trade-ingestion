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

@Service
@RequiredArgsConstructor
@Slf4j
public class DealIngestionService {

    private final DealRepository dealRepository;
    private final DealValidationService dealValidationService;
    private final DealTransformationService dealTransformationService;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    @Transactional
    @Timed(value = "process.deal", description = "Time taken to process a deal")
    public Deal processDeal(Deal deal) {
        log.info("Processing deal: {}", deal.getDealId());
        
        // Validate deal
        dealValidationService.validateDealOrThrow(deal);
        
        // Transform deal
        Deal transformedDeal = dealTransformationService.transformDeal(deal);
        
        // Save deal with circuit breaker and retry
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("dealProcessing");
        Retry retry = retryRegistry.retry("dealProcessing");
        
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

    @Cacheable(value = "deals", key = "#id")
    public Optional<Deal> getDealById(UUID id) {
        log.info("Retrieving deal by ID: {}", id);
        return dealRepository.findById(id);
    }

    @Cacheable(value = "deals", key = "'all'")
    public List<Deal> getAllDeals() {
        log.info("Retrieving all deals");
        return dealRepository.findAll();
    }

    @Cacheable(value = "deals", key = "#symbol")
    public List<Deal> getDealsBySymbol(String symbol) {
        log.info("Retrieving deals by symbol: {}", symbol);
        return dealRepository.findByInstrumentId(symbol);
    }

    @CacheEvict(value = "deals", allEntries = true)
    public void clearCache() {
        log.info("Clearing deals cache");
    }
} 