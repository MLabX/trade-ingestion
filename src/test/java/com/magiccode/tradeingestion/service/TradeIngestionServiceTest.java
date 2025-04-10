package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.repository.DealRepository;
import com.magiccode.tradeingestion.exception.DealProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealIngestionServiceTest {

    @Mock
    private DealRepository dealRepository;

    @Mock
    private DealValidationService dealValidationService;

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private DealIngestionService dealIngestionService;

    private Deal testDeal;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        testDeal = Deal.createNew(
            "DEAL-123",
            "COUNTERPARTY-1",
            "AAPL",
            new BigDecimal("100"),
            new BigDecimal("150.50"),
            "USD"
        );
    }

    @Test
    void testProcessDeal_Success() {
        // Arrange
        when(dealValidationService.validateDeal(any(Deal.class))).thenReturn(Collections.emptyList());
        when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);

        // Act
        Deal result = dealIngestionService.processDeal(testDeal);

        // Assert
        assertNotNull(result);
        assertEquals(testDeal.getInstrumentId(), result.getInstrumentId());
        assertEquals(testDeal.getQuantity(), result.getQuantity());
        assertEquals(testDeal.getPrice(), result.getPrice());
        verify(dealRepository).save(any(Deal.class));
        verify(jmsTemplate).convertAndSend(eq("deals"), any(Deal.class));
    }

    @Test
    void testProcessDeal_ValidationFailure() {
        // Arrange
        List<String> validationErrors = Arrays.asList("Invalid client ID", "Invalid instrument ID");
        when(dealValidationService.validateDeal(any(Deal.class))).thenReturn(validationErrors);

        // Act & Assert
        assertThrows(DealProcessingException.class, () -> dealIngestionService.processDeal(testDeal));
        verify(dealRepository, never()).save(any(Deal.class));
        verify(jmsTemplate, never()).convertAndSend(anyString(), any(Deal.class));
    }

    @Test
    void testGetDealById_Success() {
        // Arrange
        when(dealRepository.findById(testDeal.getId())).thenReturn(Optional.of(testDeal));

        // Act
        Deal result = dealIngestionService.getDealById(testDeal.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testDeal.getId(), result.getId());
        assertEquals(testDeal.getInstrumentId(), result.getInstrumentId());
    }

    @Test
    void testGetDealById_NotFound() {
        // Arrange
        when(dealRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DealProcessingException.class, () -> dealIngestionService.getDealById(UUID.randomUUID()));
    }

    @Test
    void testGetAllDeals_Success() {
        // Arrange
        Deal deal2 = Deal.createNew(
            "DEAL-124",
            "COUNTERPARTY-2",
            "GOOGL",
            new BigDecimal("200"),
            new BigDecimal("2500.75"),
            "USD"
        );
        when(dealRepository.findAll()).thenReturn(Arrays.asList(testDeal, deal2));

        // Act
        List<Deal> results = dealIngestionService.getAllDeals();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void testGetDealsBySymbol_Success() {
        // Arrange
        when(dealRepository.findByInstrumentId("AAPL")).thenReturn(Arrays.asList(testDeal));

        // Act
        List<Deal> results = dealIngestionService.getDealsBySymbol("AAPL");

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("AAPL", results.get(0).getInstrumentId());
    }
} 