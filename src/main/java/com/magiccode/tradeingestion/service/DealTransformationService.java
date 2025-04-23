package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.exception.DealProcessingException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealTransformationService {

    private final WebClient webClient;
    
    @Value("${deal.transformation.service.url:http://localhost:8082/api/transform}")
    private String transformationServiceUrl;

    @Timed(value = "deal.transformation.time", description = "Time taken to transform a deal")
    @CircuitBreaker(name = "dealTransformation", fallbackMethod = "transformDealFallback")
    public Deal transformDeal(Deal deal) {
        log.info("Transforming deal: {}", deal.getDealId());
        
        return webClient.post()
                .uri(transformationServiceUrl)
                .bodyValue(deal)
                .retrieve()
                .bodyToMono(Deal.class)
                .block();
    }
    
    public Deal transformDealFallback(Deal deal, Exception e) {
        log.error("Error transforming deal: {}. Using fallback method.", deal.getDealId(), e);
        return deal;
    }
    
    public Mono<Deal> transformDealAsync(Deal deal) {
        log.info("Asynchronously transforming deal: {}", deal.getDealId());
        
        return webClient.post()
                .uri(transformationServiceUrl)
                .bodyValue(deal)
                .retrieve()
                .bodyToMono(Deal.class)
                .onErrorMap(e -> new DealProcessingException("Async deal transformation failed", e));
    }
} 