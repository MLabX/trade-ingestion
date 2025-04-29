package com.magiccode.tradeingestion.util;

import com.magiccode.tradeingestion.model.FixedIncomeDerivativeDeal;
import com.magiccode.tradeingestion.model.DealLeg;
import com.magiccode.tradeingestion.model.BookingInfo;
import com.magiccode.tradeingestion.model.TraderInfo;
import com.magiccode.tradeingestion.model.CounterpartyInfo;
import com.magiccode.tradeingestion.model.NotionalAmount;
import com.magiccode.tradeingestion.repository.DealRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DatabaseTestUtilsTest {

    @Autowired
    private DatabaseTestUtils dbUtils;

    @Autowired
    private DealRepository dealRepository;

    private FixedIncomeDerivativeDeal testDeal;

    @BeforeEach
    public void setUp() {
        // Clean and migrate the database
        dbUtils.cleanAndMigrate();

        // Create test deal legs
        DealLeg payLeg = DealLeg.builder()
            .legId("L1")
            .legType("FIXED")
            .legCurrency("AUD")
            .notionalAmount(new NotionalAmount(new BigDecimal("10000000"), "AUD"))
            .fixedRate(new BigDecimal("0.0375"))
            .paymentFrequency("P6M")
            .dayCountConvention("ACT/365")
            .businessDayConvention("ModifiedFollowing")
            .build();

        DealLeg receiveLeg = DealLeg.builder()
            .legId("L2")
            .legType("FLOATING")
            .legCurrency("USD")
            .notionalAmount(new NotionalAmount(new BigDecimal("7500000"), "USD"))
            .floatingRateIndex("LIBOR")
            .floatingRateSpread(new BigDecimal("0.0015"))
            .paymentFrequency("P3M")
            .dayCountConvention("ACT/360")
            .businessDayConvention("ModifiedFollowing")
            .build();

        // Create a test deal
        testDeal = FixedIncomeDerivativeDeal.builder()
            .dealId("FID-123")
            .dealType("InterestRateSwap")
            .executionVenue("OTC")
            .tradeDate(LocalDate.now())
            .valueDate(LocalDate.now().plusDays(2))
            .maturityDate(LocalDate.now().plusYears(5))
            .status("NEW")
            .isBackDated(false)
            .bookingInfo(BookingInfo.builder()
                .id(null)
                .books(List.of(BookingInfo.Book.builder()
                    .id(null)
                    .bookCode("FIC-IRSYD01")
                    .bookName("Sydney IRS")
                    .bookType("Trading")
                    .bookCurrency("AUD")
                    .build()))
                .build())
            .trader(TraderInfo.builder()
                .id("TR123")
                .name("John Doe")
                .desk("Rates-Sydney")
                .build())
            .counterparty(CounterpartyInfo.builder()
                .entityId("CP-987654")
                .legalName("ABC Bank")
                .lei("5493001KJTIIGC8Y1R12")
                .jurisdiction("AU")
                .build())
            .legs(List.of(payLeg, receiveLeg))
            .version(1L)
            .build();
    }

    @Test
    public void testTableStats() {
        // Save the deal
        dealRepository.save(testDeal);
        dbUtils.flushAndClear();

        // Get table statistics
        Map<String, Object> stats = dbUtils.getTableStats("fixed_income_derivative_deal");
        
        assertNotNull(stats);
        assertEquals(1L, stats.get("row_count"));
        assertNotNull(stats.get("total_size"));
        assertNotNull(stats.get("table_size"));
        assertNotNull(stats.get("index_size"));
    }

    @Test
    public void testActiveDealsView() {
        // Save the deal
        dealRepository.save(testDeal);
        dbUtils.flushAndClear();

        // Query active deals view
        List<String> activeDeals = dbUtils.getActiveDeals();
        
        assertNotNull(activeDeals);
        assertEquals(1, activeDeals.size());
        assertEquals("FID-123", activeDeals.get(0));
    }

    @Test
    public void testDealsByCurrencyView() {
        // Save the deal
        dealRepository.save(testDeal);
        dbUtils.flushAndClear();

        // Query deals by currency view
        Map<String, Long> dealsByCurrency = dbUtils.getDealsByCurrency();
        
        assertNotNull(dealsByCurrency);
        assertEquals(2, dealsByCurrency.size());
        assertEquals(1L, dealsByCurrency.get("AUD"));
        assertEquals(1L, dealsByCurrency.get("USD"));
    }

    @Test
    public void testResetSequences() {
        // Save multiple deals
        dealRepository.save(testDeal);
        
        FixedIncomeDerivativeDeal anotherDeal = FixedIncomeDerivativeDeal.builder()
            .dealId("FID-124")
            .dealType("InterestRateSwap")
            .executionVenue("OTC")
            .tradeDate(LocalDate.now())
            .valueDate(LocalDate.now().plusDays(2))
            .maturityDate(LocalDate.now().plusYears(5))
            .status("NEW")
            .isBackDated(false)
            .version(1L)
            .build();
        
        dealRepository.save(anotherDeal);
        dbUtils.flushAndClear();

        // Reset sequences
        dbUtils.resetSequences();
        
        // Save a new deal after reset
        FixedIncomeDerivativeDeal newDeal = FixedIncomeDerivativeDeal.builder()
            .dealId("FID-125")
            .dealType("InterestRateSwap")
            .executionVenue("OTC")
            .tradeDate(LocalDate.now())
            .valueDate(LocalDate.now().plusDays(2))
            .maturityDate(LocalDate.now().plusYears(5))
            .status("NEW")
            .isBackDated(false)
            .version(1L)
            .build();
        
        FixedIncomeDerivativeDeal savedDeal = dealRepository.save(newDeal);
        
        // The ID should start from 1 again
        assertEquals(1L, savedDeal.getId());
    }

    @Test
    public void testClearAllTables() {
        // Save the deal
        dealRepository.save(testDeal);
        dbUtils.flushAndClear();

        // Clear all tables
        dbUtils.clearAllTables();
        
        // Verify tables are empty
        Map<String, Object> stats = dbUtils.getTableStats("fixed_income_derivative_deal");
        assertEquals(0L, stats.get("row_count"));
    }
} 