package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.BaseIntegrationTest;
import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.model.FixedIncomeDerivativeDeal;
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
@ActiveProfiles("integration")
public class DealIngestionServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DealIngestionService dealIngestionService;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private JmsTemplate jmsTemplate;

    private FixedIncomeDerivativeDeal testDeal;
    private LocalDateTime now;

    @BeforeEach
    public void setUp() {
        // Clear the database before each test
        dealRepository.deleteAll();

        // Create a test deal
        now = LocalDateTime.now();
        testDeal = FixedIncomeDerivativeDeal.createNew(
            FixedIncomeDerivativeDeal.class,
            "DEAL-123",
            "COUNTERPARTY-1",
            "AAPL",
            new BigDecimal("100"),
            new BigDecimal("150.50"),
            "USD"
        );
        testDeal.setStatus("NEW");
        testDeal.setVersion(1L);
        testDeal.setDealDate(now);
        testDeal.setCreatedAt(now);
        testDeal.setUpdatedAt(now);
        testDeal.setProcessedAt(now);
    }

    @Test
    public void testProcessDeal_SaveToDatabase() {
        // Act
        Deal savedDeal = dealIngestionService.processDeal(testDeal);

        // Assert
        assertNotNull(savedDeal.getId());
        assertEquals(testDeal.getInstrumentId(), savedDeal.getInstrumentId());
        assertEquals(testDeal.getQuantity(), savedDeal.getQuantity());
        assertEquals(testDeal.getPrice(), savedDeal.getPrice());
        assertEquals(testDeal.getStatus(), savedDeal.getStatus());

        // Verify it was saved to the database
        Deal retrievedDeal = dealRepository.findById(savedDeal.getId()).orElse(null);
        assertNotNull(retrievedDeal);
        assertEquals(savedDeal.getInstrumentId(), retrievedDeal.getInstrumentId());
    }

    @Test
    public void testGetDealById() {
        // Arrange
        Deal savedDeal = dealIngestionService.processDeal(testDeal);

        // Act
        Deal retrievedDeal = dealIngestionService.getDealById(savedDeal.getId()).orElse(null);

        // Assert
        assertNotNull(retrievedDeal);
        assertEquals(savedDeal.getId(), retrievedDeal.getId());
        assertEquals(savedDeal.getInstrumentId(), retrievedDeal.getInstrumentId());
    }

    @Test
    public void testGetAllDeals() {
        // Arrange
        dealIngestionService.processDeal(testDeal);

        // Create another deal
        FixedIncomeDerivativeDeal anotherDeal = FixedIncomeDerivativeDeal.createNew(
            FixedIncomeDerivativeDeal.class,
            "DEAL-124",
            "COUNTERPARTY-2",
            "GOOGL",
            new BigDecimal("200"),
            new BigDecimal("2500.75"),
            "USD"
        );
        anotherDeal.setStatus("NEW");
        anotherDeal.setVersion(1L);
        anotherDeal.setDealDate(now);
        anotherDeal.setCreatedAt(now);
        anotherDeal.setUpdatedAt(now);
        anotherDeal.setProcessedAt(now);
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
        FixedIncomeDerivativeDeal anotherDeal = FixedIncomeDerivativeDeal.createNew(
            FixedIncomeDerivativeDeal.class,
            "DEAL-124",
            "COUNTERPARTY-2",
            "GOOGL",
            new BigDecimal("200"),
            new BigDecimal("2500.75"),
            "USD"
        );
        anotherDeal.setStatus("NEW");
        anotherDeal.setVersion(1L);
        anotherDeal.setDealDate(now);
        anotherDeal.setCreatedAt(now);
        anotherDeal.setUpdatedAt(now);
        anotherDeal.setProcessedAt(now);
        dealIngestionService.processDeal(anotherDeal);

        // Act
        List<Deal> deals = dealIngestionService.getDealsBySymbol("AAPL");

        // Assert
        assertNotNull(deals);
        assertEquals(1, deals.size());
        assertEquals("AAPL", deals.get(0).getInstrumentId());
    }
} 