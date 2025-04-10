package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.exception.DealProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DealIdGeneratorService {
    private static final Logger logger = LoggerFactory.getLogger(DealIdGeneratorService.class);
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private final String dealIdServiceUrl;
    private final RestTemplate restTemplate;
    private final ExecutorService executorService;

    public DealIdGeneratorService(
            @Value("${deal.id.service.url:http://localhost:8082/api/deal-ids}") String dealIdServiceUrl) {
        this.dealIdServiceUrl = dealIdServiceUrl;
        this.restTemplate = new RestTemplate();
        this.executorService = Executors.newFixedThreadPool(5);
    }

    /**
     * Generates a deal ID synchronously with retry logic
     */
    public String generateDealId(String counterpartyId) {
        return generateDealIdWithRetry(counterpartyId);
    }

    /**
     * Generates a deal ID asynchronously
     */
    public CompletableFuture<String> generateDealIdAsync(String counterpartyId) {
        return CompletableFuture.supplyAsync(() -> generateDealId(counterpartyId), executorService);
    }

    /**
     * Generates a deal ID with retry logic
     */
    private String generateDealIdWithRetry(String counterpartyId) {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                logger.info("Requesting deal ID for counterparty: {}", counterpartyId);
                String dealId = restTemplate.postForObject(
                    dealIdServiceUrl,
                    new DealIdRequest(counterpartyId),
                    String.class
                );

                if (dealId == null || dealId.isEmpty()) {
                    throw new DealProcessingException("Received empty deal ID from service");
                }

                logger.info("Generated deal ID: {} for counterparty: {}", dealId, counterpartyId);
                return dealId;

            } catch (Exception e) {
                attempts++;
                logger.warn("Failed to generate deal ID (attempt {}/{}): {}",
                    attempts, MAX_RETRIES, e.getMessage());

                if (attempts == MAX_RETRIES) {
                    throw new DealProcessingException("Failed to generate deal ID after " + MAX_RETRIES + " attempts", e);
                }

                try {
                    Thread.sleep(RETRY_DELAY_MS * attempts);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new DealProcessingException("Deal ID generation interrupted", ie);
                }
            }
        }

        throw new DealProcessingException("Failed to generate deal ID");
    }

    // Java 21 record pattern
    private record DealIdRequest(String counterpartyId) {
    }
} 