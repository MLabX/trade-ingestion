package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.exception.DealProcessingException;
import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.repository.DealRepository;
import com.magiccode.tradeingestion.exception.DealProcessingException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealIngestionService {

    private final DealRepository dealRepository;
    private final DealValidationService dealValidationService;
    private final DealTransformationService dealTransformationService;
    private final JmsTemplate jmsTemplate;
    private final RestTemplate restTemplate;
    private final CircuitBreakerFactory circuitBreakerFactory;

    @Timed(value = "process.deal", description = "Time taken to process a deal")
    @CircuitBreaker(name = "dealProcessing", fallbackMethod = "processDealFallback")
    @RateLimiter(name = "dealProcessing")
    @Retry(name = "dealProcessing")
    @Transactional
    public Deal processDeal(Deal deal) {
        log.info("Processing deal: {}", deal.getDealId());
        
        // Validate the deal
        List<String> validationErrors = dealValidationService.validateDeal(deal);
        if (!validationErrors.isEmpty()) {
            log.error("Deal validation failed: {}", validationErrors);
            throw new DealProcessingException("Deal validation failed: " + String.join(", ", validationErrors));
        }

        // Transform the deal
        Deal transformedDeal = dealTransformationService.transformDeal(deal);
        
        // Check for version conflicts
        Deal existingDeal = dealRepository.findByDealId(deal.getDealId()).orElse(null);
        if (existingDeal != null && !existingDeal.getVersion().equals(deal.getVersion())) {
            log.error("Version conflict detected for deal: {}", deal.getDealId());
            throw new DealProcessingException("Version conflict detected");
        }

        // Save the deal
        Deal savedDeal = dealRepository.save(transformedDeal);
        
        // Send to queue
        jmsTemplate.convertAndSend("deals", savedDeal);
        
        log.info("Deal processed successfully: {}", savedDeal.getDealId());
        return savedDeal;
    }

    public Deal processDealFallback(Deal deal, Exception e) {
        log.error("Fallback triggered for deal: {}", deal.getDealId(), e);
        // Implement fallback logic (e.g., save to dead letter queue)
        return deal;
    }

    @Timed(value = "get.deal", description = "Time taken to get a deal by ID")
    @Cacheable(value = "deals", key = "#id")
    public Deal getDealById(UUID id) {
        return dealRepository.findById(id)
            .orElseThrow(() -> new DealProcessingException("Deal not found with id: " + id));
    }

    @Timed(value = "get.all.deals", description = "Time taken to get all deals")
    @Cacheable(value = "allDeals")
    public List<Deal> getAllDeals() {
        return dealRepository.findAll();
    }

    public List<Deal> getDealByInstrumentId(String instrumentId) {
        return dealRepository.findByInstrumentId(instrumentId);
    }

    @Timed(value = "get.deals.by.symbol", description = "Time taken to get deals by symbol")
    @Cacheable(value = "dealsBySymbol", key = "#symbol")
    public List<Deal> getDealsBySymbol(String symbol) {
        return dealRepository.findByInstrumentId(symbol);
    }

    @CacheEvict(value = {"deals", "allDeals", "dealsBySymbol"}, allEntries = true)
    public void clearCache() {
        log.info("Cache cleared");
    }
} 