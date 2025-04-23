package com.magiccode.tradeingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for the Trade Ingestion Service.
 * 
 * This service is responsible for:
 * 1. Ingesting trade deals from various sources
 * 2. Processing and validating deal data
 * 3. Storing deals in the database
 * 4. Providing APIs for deal retrieval and management
 * 
 * Key features enabled:
 * - @EnableCaching: Enables Spring's caching abstraction for improved performance
 * - @EnableAsync: Enables asynchronous processing for better scalability
 * 
 * The application follows a microservices architecture with:
 * - RESTful APIs for deal management
 * - Message-driven processing for deal ingestion
 * - Caching for frequently accessed data
 * - Asynchronous processing for long-running operations
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class DealIngestionApplication {
    /**
     * Main entry point for the application.
     * 
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(DealIngestionApplication.class, args);
    }
} 