package com.magiccode.tradeingestion.testdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.model.FixedIncomeDerivativeDeal;
import com.magiccode.tradeingestion.model.TestDeal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.text.SimpleDateFormat;

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
public class TestDataFactory {
    // Single ObjectMapper instance for thread-safe JSON processing
    private static final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .findAndRegisterModules()
        .setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"))
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    
    // Formatters for consistent date/time handling
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;
    
    /**
     * Cache for raw JSON content.
     * Key: File name
     * Value: JSON string content
     * 
     * This cache prevents repeated file I/O operations which can be expensive,
     * especially in containerized environments or when running multiple tests.
     */
    private static final Map<String, String> jsonCache = new ConcurrentHashMap<>();
    
    /**
     * Cache for deserialized objects.
     * Key: File name + Class name
     * Value: Deserialized object
     * 
     * This cache prevents repeated deserialization of the same JSON content,
     * which can be CPU-intensive for complex objects.
     */
    private static final Map<String, Object> objectCache = new ConcurrentHashMap<>();

    /**
     * Creates a test deal instance.
     * Uses caching to optimize performance in test environments.
     * 
     * @return A new TestDeal instance
     */
    public static TestDeal createTestDeal() {
        return getOrCreate("base-deal.json", TestDeal.class);
    }

    /**
     * Creates a fixed income derivative deal instance.
     * Uses caching to optimize performance in test environments.
     * 
     * @return A new FixedIncomeDerivativeDeal instance
     */
    public static FixedIncomeDerivativeDeal createFixedIncomeDerivativeDeal() {
        return getOrCreate("fixed-income-derivative-deal.json", FixedIncomeDerivativeDeal.class);
    }

    /**
     * Internal method to get or create a test object.
     * Implements the double-caching strategy:
     * 1. First checks object cache
     * 2. If not found, gets JSON from cache or loads from file
     * 3. Deserializes and caches the object
     * 
     * @param fileName The name of the JSON file
     * @param clazz The target class type
     * @return A cached or newly created instance
     */
    @SuppressWarnings("unchecked")
    private static <T> T getOrCreate(String fileName, Class<T> clazz) {
        String cacheKey = fileName + "_" + clazz.getName();
        
        // Check object cache first to avoid deserialization
        T cachedObject = (T) objectCache.get(cacheKey);
        if (cachedObject != null) {
            return cachedObject;
        }
        
        try {
            // Get JSON content from cache or load from file
            // computeIfAbsent is atomic and thread-safe
            String json = jsonCache.computeIfAbsent(fileName, key -> {
                try {
                    return loadJson("test-data/deals/" + key);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to load JSON file: " + key, e);
                }
            });
            
            // Deserialize and cache the object
            T object = objectMapper.readValue(json, clazz);
            objectCache.put(cacheKey, object);
            return object;
        } catch (IOException e) {
            log.error("Error creating test object for file: {}", fileName, e);
            throw new RuntimeException("Failed to create test object", e);
        }
    }

    /**
     * Loads JSON content from a classpath resource.
     * This method is only called when the content is not in the cache.
     * 
     * @param path The path to the JSON file
     * @return The JSON content as a string
     * @throws IOException If the file cannot be read
     */
    private static String loadJson(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }

    /**
     * Factory method to create a deal of the specified type.
     * Uses the caching mechanism to optimize performance.
     * 
     * @param dealClass The type of deal to create
     * @return A new deal instance
     * @throws IllegalArgumentException If the deal type is not supported
     */
    public static <T extends Deal> T createDeal(Class<T> dealClass) {
        if (dealClass == TestDeal.class) {
            return (T) createTestDeal();
        } else if (dealClass == FixedIncomeDerivativeDeal.class) {
            return (T) createFixedIncomeDerivativeDeal();
        }
        throw new IllegalArgumentException("Unsupported deal type: " + dealClass.getSimpleName());
    }
    
    /**
     * Clears both caches.
     * This method should be called between test classes to prevent memory leaks
     * and ensure test isolation.
     */
    public static void clearCache() {
        jsonCache.clear();
        objectCache.clear();
    }
} 