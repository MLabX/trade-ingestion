package com.magiccode.tradeingestion.testdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.model.FixedIncomeDerivativeDeal;
import com.magiccode.tradeingestion.model.TestDeal;
import com.magiccode.tradeingestion.model.MessageHeader;
import com.magiccode.tradeingestion.model.RiskMetrics;
import com.magiccode.tradeingestion.model.ValuationInfo;
import com.magiccode.tradeingestion.model.CounterpartyInfo;
import com.magiccode.tradeingestion.model.DealLifecycleEvent;
import com.magiccode.tradeingestion.model.AuditInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Factory class for creating test data objects.
 * 
 * This class implements a double-caching strategy to optimize test performance:
 * 1. JSON content caching: Prevents repeated file I/O operations
 * 2. Object caching: Prevents repeated deserialization
 * 
 * The caching is particularly important for CI/CD environments where:
 * - Multiple test classes may run in parallel
 * - File system operations can be slower than local development
 * - Memory usage needs to be optimized
 * 
 * Thread safety is ensured through ConcurrentHashMap to support parallel test execution.
 */
@Slf4j
@Component
public class TestDataFactory {
    private final ObjectMapper objectMapper;
    private final TestDataConfig testDataConfig;

    // Cache for JSON content to avoid repeated file I/O
    private final ConcurrentHashMap<String, String> jsonContentCache = new ConcurrentHashMap<>();

    // Cache for deserialized objects to avoid repeated deserialization
    private final ConcurrentHashMap<String, Object> objectCache = new ConcurrentHashMap<>();

    @Autowired
    public TestDataFactory(ObjectMapper objectMapper, TestDataConfig testDataConfig) {
        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.testDataConfig = testDataConfig;
    }

    /**
     * Creates a test deal instance.
     * Uses caching to optimize performance in test environments.
     * 
     * @return A new TestDeal instance
     * @throws TestDataException if the deal cannot be created
     */
    public TestDeal createTestDeal() {
        try {
            return createDeal(testDataConfig.getBaseDealPath(), TestDeal.class);
        } catch (Exception e) {
            log.error("Failed to create test deal", e);
            throw new TestDataException("Failed to create test deal", e);
        }
    }

    /**
     * Creates a fixed income derivative deal instance.
     * Uses caching to optimize performance in test environments.
     * 
     * @return A new FixedIncomeDerivativeDeal instance
     * @throws TestDataException if the deal cannot be created
     */
    public FixedIncomeDerivativeDeal createFixedIncomeDerivativeDeal() {
        try {
            return createDeal(testDataConfig.getFixedIncomeDerivativeDealPath(), FixedIncomeDerivativeDeal.class);
        } catch (Exception e) {
            log.error("Failed to create fixed income derivative deal", e);
            throw new TestDataException("Failed to create fixed income derivative deal", e);
        }
    }

    /**
     * Creates a new fixed income derivative deal instance.
     * Uses caching to optimize performance in test environments.
     * 
     * @return A new FixedIncomeDerivativeDeal instance with NEW status
     * @throws TestDataException if the deal cannot be created
     */
    public FixedIncomeDerivativeDeal createNewFixedIncomeDerivativeDeal() {
        try {
            return createDeal(testDataConfig.getNewDealPath(), FixedIncomeDerivativeDeal.class);
        } catch (Exception e) {
            log.error("Failed to create new fixed income derivative deal", e);
            throw new TestDataException("Failed to create new fixed income derivative deal", e);
        }
    }

    /**
     * Creates a confirmed fixed income derivative deal instance.
     * Uses caching to optimize performance in test environments.
     * 
     * @return A new FixedIncomeDerivativeDeal instance with CONFIRMED status
     * @throws TestDataException if the deal cannot be created
     */
    public FixedIncomeDerivativeDeal createConfirmedFixedIncomeDerivativeDeal() {
        try {
            return createDeal(testDataConfig.getConfirmedDealPath(), FixedIncomeDerivativeDeal.class);
        } catch (Exception e) {
            log.error("Failed to create confirmed fixed income derivative deal", e);
            throw new TestDataException("Failed to create confirmed fixed income derivative deal", e);
        }
    }

    /**
     * Creates an amended fixed income derivative deal instance.
     * Uses caching to optimize performance in test environments.
     * 
     * @return A new FixedIncomeDerivativeDeal instance with AMENDED status
     * @throws TestDataException if the deal cannot be created
     */
    public FixedIncomeDerivativeDeal createAmendedFixedIncomeDerivativeDeal() {
        try {
            return createDeal(testDataConfig.getAmendedDealPath(), FixedIncomeDerivativeDeal.class);
        } catch (Exception e) {
            log.error("Failed to create amended fixed income derivative deal", e);
            throw new TestDataException("Failed to create amended fixed income derivative deal", e);
        }
    }

    /**
     * Creates a cancelled fixed income derivative deal instance.
     * Uses caching to optimize performance in test environments.
     * 
     * @return A new FixedIncomeDerivativeDeal instance with CANCELLED status
     * @throws TestDataException if the deal cannot be created
     */
    public FixedIncomeDerivativeDeal createCancelledFixedIncomeDerivativeDeal() {
        try {
            return createDeal(testDataConfig.getCancelledDealPath(), FixedIncomeDerivativeDeal.class);
        } catch (Exception e) {
            log.error("Failed to create cancelled fixed income derivative deal", e);
            throw new TestDataException("Failed to create cancelled fixed income derivative deal", e);
        }
    }

    /**
     * Creates a deal with custom modifications.
     * 
     * @param dealClass The type of deal to create
     * @param modifier A consumer that can modify the deal after creation
     * @return A new deal instance with custom modifications
     * @throws TestDataException if the deal cannot be created or modified
     */
    public <T extends Deal> T createDeal(Class<T> dealClass, Consumer<T> modifier) {
        try {
            T deal = createDeal(dealClass);
            modifier.accept(deal);
            return deal;
        } catch (Exception e) {
            log.error("Failed to create or modify deal of type {}", dealClass.getSimpleName(), e);
            throw new TestDataException("Failed to create or modify deal", e);
        }
    }

    /**
     * Factory method to create a deal of the specified type.
     * Uses the caching mechanism to optimize performance.
     * 
     * @param dealClass The type of deal to create
     * @return A new deal instance
     * @throws TestDataException if the deal cannot be created
     */
    public <T extends Deal> T createDeal(Class<T> dealClass) {
        if (dealClass == TestDeal.class) {
            return (T) createTestDeal();
        } else if (dealClass == FixedIncomeDerivativeDeal.class) {
            return (T) createFixedIncomeDerivativeDeal();
        }
        throw new TestDataException("Unsupported deal type: " + dealClass.getSimpleName());
    }

    /**
     * Internal method to get or create a test object.
     * Implements the double-caching strategy:
     * 1. First checks object cache
     * 2. If not found, gets JSON from cache or loads from file
     * 3. Deserializes and caches the object
     * 
     * @param filePath The path to the JSON file
     * @param dealClass The type of deal to create
     * @return A cached or newly created instance
     * @throws TestDataException if the deal cannot be created
     */
    private <T extends Deal> T createDeal(String filePath, Class<T> dealClass) {
        try {
            return dealClass.cast(objectCache.computeIfAbsent(filePath, key -> {
                String jsonContent = jsonContentCache.computeIfAbsent(filePath, this::loadJsonFromClasspath);
                try {
                    Map<String, Object> jsonMap = objectMapper.readValue(jsonContent, new TypeReference<Map<String, Object>>() {});
                    T deal = objectMapper.convertValue(jsonMap, dealClass);
                    validateDeal(deal);
                    return deal;
                } catch (Exception e) {
                    log.error("Failed to deserialize deal from JSON: {}", filePath, e);
                    throw new TestDataException("Failed to deserialize deal from JSON: " + filePath, e);
                }
            }));
        } catch (Exception e) {
            log.error("Failed to create deal from file: {}", filePath, e);
            throw new TestDataException("Failed to create deal from file: " + filePath, e);
        }
    }

    /**
     * Loads JSON content from a classpath resource.
     * This method is only called when the content is not in the cache.
     * 
     * @param filePath The path to the JSON file
     * @return The JSON content as a string
     * @throws TestDataException if the file cannot be read
     */
    private String loadJsonFromClasspath(String filePath) {
        ClassPathResource resource = new ClassPathResource(filePath);
        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (Exception e) {
            log.error("Failed to load JSON from classpath: {}", filePath, e);
            throw new TestDataException("Failed to load JSON from classpath: " + filePath, e);
        }
    }

    /**
     * Validates a deal to ensure it meets minimum requirements.
     * 
     * @param deal The deal to validate
     * @throws TestDataException if the deal is invalid
     */
    private void validateDeal(Deal deal) {
        if (deal == null) {
            throw new TestDataException("Deal cannot be null");
        }
        if (deal.getDealId() == null || deal.getDealId().isEmpty()) {
            throw new TestDataException("Deal ID is required");
        }
        if (deal.getDealType() == null || deal.getDealType().isEmpty()) {
            throw new TestDataException("Deal type is required");
        }
    }

    /**
     * Clears both caches.
     * This method should be called between test classes to prevent memory leaks
     * and ensure test isolation.
     */
    public void clearCache() {
        jsonContentCache.clear();
        objectCache.clear();
        log.debug("Test data caches cleared");
    }
}

/**
 * Custom exception for test data related errors.
 */
class TestDataException extends RuntimeException {
    public TestDataException(String message) {
        super(message);
    }

    public TestDataException(String message, Throwable cause) {
        super(message, cause);
    }
} 
