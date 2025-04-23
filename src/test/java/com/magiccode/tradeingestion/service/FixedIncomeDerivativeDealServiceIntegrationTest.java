package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.config.TestConfig;
import com.magiccode.tradeingestion.model.FixedIncomeDerivativeDeal;
import com.magiccode.tradeingestion.model.DealLeg;
import com.magiccode.tradeingestion.model.BookingInfo;
import com.magiccode.tradeingestion.model.TraderInfo;
import com.magiccode.tradeingestion.model.CounterpartyInfo;
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
public class FixedIncomeDerivativeDealServiceIntegrationTest {

    @Autowired
    private FixedIncomeDerivativeDealService dealService;

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

        // Create test deal legs
        DealLeg payLeg = DealLeg.builder()
            .legId("L1")
            .payOrReceive("Pay")
            .notionalAmount(new DealLeg.NotionalAmount(new BigDecimal("10000000"), "AUD"))
            .legType("Fixed")
            .legRate(new BigDecimal("0.0375"))
            .legPaymentFrequency("P6M")
            .legDayCountConvention("ACT/365")
            .legBusinessDayConvention("ModifiedFollowing")
            .build();

        DealLeg receiveLeg = DealLeg.builder()
            .legId("L2")
            .payOrReceive("Receive")
            .notionalAmount(new DealLeg.NotionalAmount(new BigDecimal("10000000"), "AUD"))
            .legType("Floating")
            .legIndex("BBSW")
            .legSpread(new BigDecimal("0.0015"))
            .legPaymentFrequency("P3M")
            .legDayCountConvention("ACT/365")
            .legBusinessDayConvention("ModifiedFollowing")
            .build();

        // Create a test deal
        now = LocalDateTime.now();
        testDeal = FixedIncomeDerivativeDeal.builder()
            .dealId("FID-123")
            .dealType("InterestRateSwap")
            .executionVenue("OTC")
            .tradeDate(now.toLocalDate())
            .valueDate(now.toLocalDate().plusDays(2))
            .maturityDate(now.toLocalDate().plusYears(5))
            .status("NEW")
            .isBackDated(false)
            .bookingInfo(BookingInfo.builder()
                .books(List.of(new BookingInfo.Book("FIC-IRSYD01", "Sydney IRS", "Trading", "AUD")))
                .build())
            .trader(TraderInfo.builder()
                .id("TR123")
                .name("Encrypted (AES256)")
                .desk("Rates-Sydney")
                .build())
            .counterparty(CounterpartyInfo.builder()
                .entityId("CP-987654")
                .legalName("Encrypted (AES256)")
                .lei("5493001KJTIIGC8Y1R12")
                .jurisdiction("AU")
                .build())
            .legs(List.of(payLeg, receiveLeg))
            .version(1L)
            .build();
    }

    @Test
    public void testProcessDeal_SaveToDatabase() {
        // Act
        FixedIncomeDerivativeDeal savedDeal = dealService.processDeal(testDeal);

        // Assert
        assertNotNull(savedDeal.getId());
        assertEquals(testDeal.getDealId(), savedDeal.getDealId());
        assertEquals(testDeal.getDealType(), savedDeal.getDealType());
        assertEquals(testDeal.getStatus(), savedDeal.getStatus());

        // Verify it was saved to the database
        FixedIncomeDerivativeDeal retrievedDeal = dealService.getDealById(savedDeal.getId());
        assertNotNull(retrievedDeal);
        assertEquals(savedDeal.getDealId(), retrievedDeal.getDealId());
    }

    @Test
    public void testGetDealById() {
        // Arrange
        FixedIncomeDerivativeDeal savedDeal = dealService.processDeal(testDeal);

        // Act
        FixedIncomeDerivativeDeal retrievedDeal = dealService.getDealById(savedDeal.getId());

        // Assert
        assertNotNull(retrievedDeal);
        assertEquals(savedDeal.getId(), retrievedDeal.getId());
        assertEquals(savedDeal.getDealId(), retrievedDeal.getDealId());
    }

    @Test
    public void testGetAllDeals() {
        // Arrange
        dealService.processDeal(testDeal);

        // Create another deal
        FixedIncomeDerivativeDeal anotherDeal = FixedIncomeDerivativeDeal.createNew(
            "DEAL-124",
            "InterestRateSwap",
            "OTC",
            now.toLocalDate(),
            now.toLocalDate().plusDays(2),
            now.toLocalDate().plusYears(5),
            "NEW",
            false,
            null, // bookingInfo
            null, // trader
            null, // counterparty
            null  // legs
        );
        anotherDeal.setVersion(1L);
        anotherDeal.setCreatedAt(now);
        anotherDeal.setUpdatedAt(now);
        anotherDeal.setProcessedAt(now);
        dealService.processDeal(anotherDeal);

        // Act
        List<FixedIncomeDerivativeDeal> deals = dealService.getAllDeals();

        // Assert
        assertNotNull(deals);
        assertEquals(2, deals.size());
    }

    @Test
    public void testGetDealsByType() {
        // Arrange
        dealService.processDeal(testDeal);

        // Create another deal with different type
        FixedIncomeDerivativeDeal anotherDeal = FixedIncomeDerivativeDeal.createNew(
            "DEAL-124",
            "CrossCurrencySwap",
            "OTC",
            now.toLocalDate(),
            now.toLocalDate().plusDays(2),
            now.toLocalDate().plusYears(5),
            "NEW",
            false,
            null, // bookingInfo
            null, // trader
            null, // counterparty
            null  // legs
        );
        anotherDeal.setVersion(1L);
        anotherDeal.setCreatedAt(now);
        anotherDeal.setUpdatedAt(now);
        anotherDeal.setProcessedAt(now);
        dealService.processDeal(anotherDeal);

        // Act
        List<FixedIncomeDerivativeDeal> deals = dealService.getDealsByType("InterestRateSwap");

        // Assert
        assertNotNull(deals);
        assertEquals(1, deals.size());
        assertEquals("InterestRateSwap", deals.get(0).getDealType());
    }
} 