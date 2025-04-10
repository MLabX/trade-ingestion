package com.magiccode.tradeingestion.controller;

import com.magiccode.tradeingestion.model.Deal;
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
import java.time.LocalDateTime;
import java.util.UUID;

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
    private DealRepository DealRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Deal deal;

    @BeforeEach
    public void setUp() {
        tradeRepository.deleteAll();
        LocalDateTime now = LocalDateTime.now();
        trade = new Trade(
            UUID.randomUUID(),
            "TRADE123",
            "CP001",
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
    public void testCreateTrade() throws Exception {
        String tradeJson = objectMapper.writeValueAsString(trade);

        mockMvc.perform(post("/api/trades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tradeJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.instrumentId").value("AAPL"))
                .andExpect(jsonPath("$.quantity").value(100))
                .andExpect(jsonPath("$.price").value(150.50))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    public void testGetTradeById() throws Exception {
        Trade savedTrade = tradeRepository.save(trade);

        mockMvc.perform(get("/api/trades/{id}", savedTrade.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instrumentId").value("AAPL"))
                .andExpect(jsonPath("$.quantity").value(100))
                .andExpect(jsonPath("$.price").value(150.50));
    }

    @Test
    public void testGetAllTrades() throws Exception {
        tradeRepository.save(trade);

        mockMvc.perform(get("/api/trades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].instrumentId").value("AAPL"))
                .andExpect(jsonPath("$[0].quantity").value(100))
                .andExpect(jsonPath("$[0].price").value(150.50));
    }

    @Test
    public void testGetTradesBySymbol() throws Exception {
        tradeRepository.save(trade);

        mockMvc.perform(get("/api/trades/symbol/{symbol}", trade.instrumentId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].instrumentId").value("AAPL"))
                .andExpect(jsonPath("$[0].quantity").value(100))
                .andExpect(jsonPath("$[0].price").value(150.50));
    }
} 