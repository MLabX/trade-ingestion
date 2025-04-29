package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.BaseIntegrationTest;
import com.magiccode.tradeingestion.config.PostgresTestConfig;
import com.magiccode.tradeingestion.model.FixedIncomeDerivativeDeal;
import com.magiccode.tradeingestion.model.DealLeg;
import com.magiccode.tradeingestion.model.BookingInfo;
import com.magiccode.tradeingestion.model.TraderInfo;
import com.magiccode.tradeingestion.model.CounterpartyInfo;
import com.magiccode.tradeingestion.model.NotionalAmount;
import com.magiccode.tradeingestion.repository.DealRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("postgres")
@Import(PostgresTestConfig.class)
@Transactional
public class FixedIncomeDerivativeDealServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private FixedIncomeDerivativeDealService dealService;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Flyway flyway;

    private FixedIncomeDerivativeDeal testDeal;
    private LocalDateTime now;

    @BeforeEach
    public void setUp() {
        // Clean and migrate the database before each test
        flyway.clean();
        flyway.migrate();

        // Create test deal legs
        DealLeg payLeg = DealLeg.builder()
            .legId("PAY-1")
            .legType("PAY")
            .legCurrency("USD")
            .notionalAmount(new NotionalAmount(BigDecimal.valueOf(1000000), "USD"))
            .fixedRate(BigDecimal.valueOf(0.05))
            .build();

        DealLeg receiveLeg = DealLeg.builder()
            .legId("REC-1")
            .legType("RECEIVE")
            .legCurrency("EUR")
            .notionalAmount(new NotionalAmount(BigDecimal.valueOf(900000), "EUR"))
            .floatingRateIndex("EURIBOR")
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
                .books(List.of(BookingInfo.Book.builder()
                    .bookCode("FIC-IRSYD01")
                    .bookName("Sydney IRS")
                    .bookType("Trading")
                    .bookCurrency("AUD")
                    .build()))
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
        FixedIncomeDerivativeDeal anotherDeal = FixedIncomeDerivativeDeal.builder()
            .dealId("DEAL-124")
            .dealType("InterestRateSwap")
            .executionVenue("OTC")
            .tradeDate(now.toLocalDate())
            .valueDate(now.toLocalDate().plusDays(2))
            .maturityDate(now.toLocalDate().plusYears(5))
            .status("NEW")
            .isBackDated(false)
            .version(1L)
            .createdAt(now)
            .updatedAt(now)
            .processedAt(now)
            .build();
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
        FixedIncomeDerivativeDeal anotherDeal = FixedIncomeDerivativeDeal.builder()
            .dealId("DEAL-124")
            .dealType("CrossCurrencySwap")
            .executionVenue("OTC")
            .tradeDate(now.toLocalDate())
            .valueDate(now.toLocalDate().plusDays(2))
            .maturityDate(now.toLocalDate().plusYears(5))
            .status("NEW")
            .isBackDated(false)
            .version(1L)
            .createdAt(now)
            .updatedAt(now)
            .processedAt(now)
            .build();
        dealService.processDeal(anotherDeal);

        // Act
        List<FixedIncomeDerivativeDeal> deals = dealService.getDealsByType("InterestRateSwap");

        // Assert
        assertNotNull(deals);
        assertEquals(1, deals.size());
        assertEquals("InterestRateSwap", deals.get(0).getDealType());
    }
} 