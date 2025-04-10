package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.config.TestConfig;
import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.repository.DealRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public class DealIngestionServiceIntegrationTest {

    @Autowired
    private DealIngestionService dealIngestionService;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private JmsTemplate jmsTemplate;

    private Deal testDeal;
    private LocalDateTime now;

    @BeforeEach
    public void setUp() {
        // Clear the database before each test
        dealRepository.deleteAll();

        // Create a test deal
        now = LocalDateTime.now();
        testDeal = Deal.createNew(
            UUID.randomUUID(),
            "DEAL-123",
            "COUNTERPARTY-1",
            "AAPL",
            new BigDecimal("100"),
            new BigDecimal("150.50"),
            "USD",
            "NEW",
            1L,
            now,
            now,
            now
        );
    }

    @Test
    public void testProcessDeal_SaveToDatabase() {
        // Act
        Deal savedDeal = dealIngestionService.processDeal(testDeal);

        // Assert
        assertNotNull(savedDeal.id());
        assertEquals(testDeal.instrumentId(), savedDeal.instrumentId());
        assertEquals(testDeal.quantity(), savedDeal.quantity());
        assertEquals(testDeal.price(), savedDeal.price());
        assertEquals(testDeal.status(), savedDeal.status());

        // Verify it was saved to the database
        Deal retrievedDeal = dealRepository.findById(savedDeal.id()).orElse(null);
        assertNotNull(retrievedDeal);
        assertEquals(savedDeal.instrumentId(), retrievedDeal.instrumentId());
    }

    @Test
    public void testGetDealById() {
        // Arrange
        Deal savedDeal = dealIngestionService.processDeal(testDeal);

        // Act
        Deal retrievedDeal = dealIngestionService.getDealById(savedDeal.id());

        // Assert
        assertNotNull(retrievedDeal);
        assertEquals(savedDeal.id(), retrievedDeal.id());
        assertEquals(savedDeal.instrumentId(), retrievedDeal.instrumentId());
    }

    @Test
    public void testGetAllDeals() {
        // Arrange
        dealIngestionService.processDeal(testDeal);

        // Create another deal
        Deal anotherDeal = Deal.createNew(
            UUID.randomUUID(),
            "DEAL-124",
            "COUNTERPARTY-2",
            "GOOGL",
            new BigDecimal("200"),
            new BigDecimal("2500.75"),
            "USD",
            "NEW",
            1L,
            now,
            now,
            now
        );
        dealIngestionService.processDeal(anotherDeal);

        // Act
        List<Deal> deals = dealIngestionService.getAllDeals();

        // Assert
        assertNotNull(deals);
        assertEquals(2, deals.size());
    }

    @Test
    public void testGetDealsBySymbol() {
        // Arrange
        dealIngestionService.processDeal(testDeal);

        // Create another deal with different symbol
        Deal anotherDeal = Deal.createNew(
            UUID.randomUUID(),
            "DEAL-124",
            "COUNTERPARTY-2",
            "GOOGL",
            new BigDecimal("200"),
            new BigDecimal("2500.75"),
            "USD",
            "NEW",
            1L,
            now,
            now,
            now
        );
        dealIngestionService.processDeal(anotherDeal);

        // Act
        List<Deal> deals = dealIngestionService.getDealsBySymbol("AAPL");

        // Assert
        assertNotNull(deals);
        assertEquals(1, deals.size());
        assertEquals("AAPL", deals.get(0).instrumentId());
    }
} 