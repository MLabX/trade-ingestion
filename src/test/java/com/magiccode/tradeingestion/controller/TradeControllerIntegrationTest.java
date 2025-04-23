package com.magiccode.tradeingestion.controller;

import com.magiccode.tradeingestion.model.*;
import com.magiccode.tradeingestion.repository.DealRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TradeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private FixedIncomeDerivativeDeal deal;

    @BeforeEach
    public void setUp() {
        dealRepository.deleteAll();
        LocalDate now = LocalDate.now();
        
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

        // Create test deal
        deal = FixedIncomeDerivativeDeal.builder()
            .dealId("IRS-20250410-00001")
            .dealType("InterestRateSwap")
            .executionVenue("OTC")
            .tradeDate(now)
            .valueDate(now.plusDays(2))
            .maturityDate(now.plusYears(5))
            .status("NEW")
            .isBackDated(false)
            .bookingInfo(BookingInfo.builder()
                .books(Collections.singletonList(new BookingInfo.Book("SYDIRS", "Sydney IRS", "Trading", "AUD")))
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
            .legs(Collections.singletonList(payLeg))
            .version(1L)
            .build();
    }

    @Test
    public void testCreateDeal() throws Exception {
        String dealJson = objectMapper.writeValueAsString(deal);

        mockMvc.perform(post("/api/deals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dealJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dealId").value("IRS-20250410-00001"))
                .andExpect(jsonPath("$.dealType").value("InterestRateSwap"))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    public void testGetDealById() throws Exception {
        Deal savedDeal = dealRepository.save(deal);

        mockMvc.perform(get("/api/deals/{id}", savedDeal.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dealId").value("IRS-20250410-00001"))
                .andExpect(jsonPath("$.dealType").value("InterestRateSwap"))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    public void testGetAllDeals() throws Exception {
        dealRepository.save(deal);

        mockMvc.perform(get("/api/deals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dealId").value("IRS-20250410-00001"))
                .andExpect(jsonPath("$[0].dealType").value("InterestRateSwap"))
                .andExpect(jsonPath("$[0].status").value("NEW"));
    }

    @Test
    public void testGetDealsBySymbol() throws Exception {
        dealRepository.save(deal);

        mockMvc.perform(get("/api/deals/symbol/{symbol}", "AUD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dealId").value("IRS-20250410-00001"))
                .andExpect(jsonPath("$[0].dealType").value("InterestRateSwap"))
                .andExpect(jsonPath("$[0].status").value("NEW"));
    }
} 