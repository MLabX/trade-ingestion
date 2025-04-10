package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.exception.DealProcessingException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class DealTransformationService {

    private final RestTemplate restTemplate;
    private static final String TRANSFORMATION_SERVICE_URL = "http://transformation-service/api/transform";

    @Autowired
    public DealTransformationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "transformDeal", fallbackMethod = "transformDealFallback")
    @RateLimiter(name = "transformDeal")
    @Retry(name = "transformDeal")
    @TimeLimiter(name = "transformDeal")
    @Timed(value = "deal.transform.time", description = "Time taken to transform deal")
    public Deal transformDeal(Deal deal) {
        log.info("Transforming deal: {}", deal.getDealId());
        try {
            return restTemplate.postForObject(TRANSFORMATION_SERVICE_URL, deal, Deal.class);
        } catch (Exception e) {
            log.error("Error transforming deal: {}", deal.getDealId(), e);
            throw new DealProcessingException("Failed to transform deal: " + deal.getDealId(), e);
        }
    }

    public Deal transformDealFallback(Deal deal, Exception e) {
        log.warn("Using fallback for deal transformation: {}", deal.getDealId());
        return deal;
    }

    @CircuitBreaker(name = "transformDealAsync", fallbackMethod = "transformDealAsyncFallback")
    @RateLimiter(name = "transformDealAsync")
    @Retry(name = "transformDealAsync")
    @TimeLimiter(name = "transformDealAsync")
    @Timed(value = "deal.transform.async.time", description = "Time taken to transform deal asynchronously")
    public CompletableFuture<Deal> transformDealAsync(Deal deal) {
        log.info("Transforming deal asynchronously: {}", deal.getDealId());
        return CompletableFuture.supplyAsync(() -> transformDeal(deal));
    }

    public CompletableFuture<Deal> transformDealAsyncFallback(Deal deal, Exception e) {
        log.warn("Using fallback for async deal transformation: {}", deal.getDealId());
        return CompletableFuture.completedFuture(deal);
    }
} 