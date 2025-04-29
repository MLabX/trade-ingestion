package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.model.*;
import com.magiccode.tradeingestion.repository.DealRepository;
import com.magiccode.tradeingestion.testdata.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the TradeIngestionService.
 * 
 * This test class demonstrates the use of the TestDataFactory for creating test data.
 * The factory's caching mechanism helps optimize test performance by:
 * 1. Reducing file I/O operations
 * 2. Minimizing object deserialization
 * 3. Supporting parallel test execution
 * 
 * The test class also includes proper cleanup to prevent memory leaks
 * and ensure test isolation.
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest
class TradeIngestionServiceTest {

    @Mock
    private DealRepository dealRepository;

    @InjectMocks
    private TradeIngestionService tradeIngestionService;

    @Autowired
    private TestDataFactory testDataFactory;

    private TestDeal testDeal;

    /**
     * Sets up test data before each test method.
     * Uses the TestDataFactory to create a test deal instance.
     * The factory's caching mechanism ensures efficient test data creation.
     */
    @BeforeEach
    void setUp() {
        testDeal = testDataFactory.createTestDeal();
        testDeal.setDealDate(LocalDateTime.now());
    }

    /**
     * Cleans up test data after each test method.
     * This is important to:
     * 1. Prevent memory leaks in long-running test suites
     * 2. Ensure test isolation between different test classes
     * 3. Maintain consistent test behavior
     */
    @AfterEach
    void tearDown() {
        testDataFactory.clearCache();
    }

    /**
     * Tests successful deal processing.
     * Verifies that:
     * 1. The deal is saved correctly
     * 2. All deal properties are preserved
     * 3. The repository's save method is called
     */
    @Test
    void testProcessDeal_Success() {
        when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);

        Deal result = tradeIngestionService.processDeal(testDeal);

        assertNotNull(result);
        assertEquals(testDeal.getDealId(), result.getDealId());
        assertEquals(testDeal.getClientId(), result.getClientId());
        assertEquals(testDeal.getInstrumentId(), result.getInstrumentId());
        assertEquals(testDeal.getQuantity(), result.getQuantity());
        assertEquals(testDeal.getPrice(), result.getPrice());
        assertEquals(testDeal.getCurrency(), result.getCurrency());
        assertEquals(testDeal.getStatus(), result.getStatus());
        assertEquals(testDeal.getVersion(), result.getVersion());
        assertEquals(testDeal.getDealDate().toLocalDate(), result.getDealDate().toLocalDate());
        verify(dealRepository).save(testDeal);
    }

    /**
     * Tests retrieving a deal by ID.
     * Verifies that:
     * 1. The correct deal is returned
     * 2. The repository's findById method is called
     */
    @Test
    void testGetDealById() {
        UUID dealId = UUID.randomUUID();
        when(dealRepository.findById(dealId)).thenReturn(Optional.of(testDeal));

        Optional<Deal> result = tradeIngestionService.getDealById(dealId);

        assertTrue(result.isPresent());
        assertEquals(testDeal, result.get());
        verify(dealRepository).findById(dealId);
    }

    /**
     * Tests retrieving all deals.
     * Verifies that:
     * 1. All deals are returned
     * 2. The repository's findAll method is called
     */
    @Test
    void testGetAllDeals() {
        List<Deal> deals = List.of(testDeal);
        when(dealRepository.findAll()).thenReturn(deals);

        List<Deal> result = tradeIngestionService.getAllDeals();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDeal, result.get(0));
        verify(dealRepository).findAll();
    }

    /**
     * Tests retrieving deals by instrument ID.
     * Verifies that:
     * 1. The correct deals are returned
     * 2. The repository's findByInstrumentId method is called
     */
    @Test
    void testGetDealsByInstrumentId() {
        String instrumentId = "TEST-INSTRUMENT";
        List<Deal> deals = List.of(testDeal);
        when(dealRepository.findByInstrumentId(instrumentId)).thenReturn(deals);

        List<Deal> result = tradeIngestionService.getDealsByInstrumentId(instrumentId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDeal, result.get(0));
        verify(dealRepository).findByInstrumentId(instrumentId);
    }

    /**
     * Tests retrieving deals by symbol.
     * Verifies that:
     * 1. The correct deals are returned
     * 2. The repository's findByInstrumentId method is called
     */
    @Test
    void testGetDealsBySymbol() {
        String instrumentId = "TEST-SYMBOL";
        List<Deal> deals = List.of(testDeal);
        when(dealRepository.findByInstrumentId(instrumentId)).thenReturn(deals);

        List<Deal> result = tradeIngestionService.getDealsBySymbol(instrumentId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDeal, result.get(0));
        verify(dealRepository).findByInstrumentId(instrumentId);
    }
}