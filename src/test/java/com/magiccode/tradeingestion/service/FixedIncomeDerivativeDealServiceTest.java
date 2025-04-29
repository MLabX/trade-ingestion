package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.exception.DealProcessingException;
import com.magiccode.tradeingestion.model.FixedIncomeDerivativeDeal;
import com.magiccode.tradeingestion.repository.FixedIncomeDerivativeDealRepository;
import com.magiccode.tradeingestion.service.transformation.DealTransformationService;
import com.magiccode.tradeingestion.service.validation.DealValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FixedIncomeDerivativeDealServiceTest {

    @Mock
    private FixedIncomeDerivativeDealRepository dealRepository;

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private DealValidationService validationService;

    @Mock
    private DealTransformationService<FixedIncomeDerivativeDeal> transformationService;

    private FixedIncomeDerivativeDealService dealService;

    private FixedIncomeDerivativeDeal testDeal;

    @BeforeEach
    void setUp() {
        dealService = new FixedIncomeDerivativeDealService(dealRepository, jmsTemplate, validationService, transformationService);
        testDeal = createTestDeal();
    }

    @Test
    void processDeal_ValidDeal_Success() {
        // Setup
        when(dealRepository.findByDealId(anyString())).thenReturn(Optional.empty());
        doNothing().when(validationService).validateDeal(any(FixedIncomeDerivativeDeal.class));
        when(transformationService.transform(any(FixedIncomeDerivativeDeal.class))).thenReturn(testDeal);
        when(dealRepository.save(any(FixedIncomeDerivativeDeal.class))).thenReturn(testDeal);

        // Execute
        FixedIncomeDerivativeDeal result = dealService.processDeal(testDeal);

        // Verify
        assertNotNull(result);
        assertEquals(testDeal.getDealId(), result.getDealId());
        verify(dealRepository).findByDealId(anyString());
        verify(validationService).validateDeal(any(FixedIncomeDerivativeDeal.class));
        verify(transformationService).transform(any(FixedIncomeDerivativeDeal.class));
        verify(dealRepository).save(any(FixedIncomeDerivativeDeal.class));
        verify(jmsTemplate).convertAndSend(eq("fixed-income-deals"), any(FixedIncomeDerivativeDeal.class));
    }

    @Test
    void processDeal_ValidationFailure_ThrowsException() {
        // Given
        doThrow(new IllegalArgumentException("Invalid deal type"))
            .when(validationService).validateDeal(any(FixedIncomeDerivativeDeal.class));

        // When/Then
        assertThrows(DealProcessingException.class, () -> dealService.processDeal(testDeal));
        verify(validationService).validateDeal(any(FixedIncomeDerivativeDeal.class));
        verifyNoMoreInteractions(dealRepository, jmsTemplate, transformationService);
    }

    @Test
    void processDeal_DuplicateDeal_ThrowsException() {
        // Setup
        when(dealRepository.findByDealId(anyString())).thenReturn(Optional.of(testDeal));
        doNothing().when(validationService).validateDeal(any(FixedIncomeDerivativeDeal.class));

        // Execute & Verify
        assertThrows(DealProcessingException.class, () -> dealService.processDeal(testDeal));
        verify(dealRepository).findByDealId(anyString());
        verify(validationService).validateDeal(any(FixedIncomeDerivativeDeal.class));
        verifyNoMoreInteractions(transformationService, dealRepository, jmsTemplate);
    }

    @Test
    void processDeal_TransformationFailure_ThrowsException() {
        // Setup
        when(dealRepository.findByDealId(anyString())).thenReturn(Optional.empty());
        doNothing().when(validationService).validateDeal(any(FixedIncomeDerivativeDeal.class));
        when(transformationService.transform(any(FixedIncomeDerivativeDeal.class)))
            .thenThrow(new DealProcessingException("Transformation failed"));

        // Execute & Verify
        assertThrows(DealProcessingException.class, () -> dealService.processDeal(testDeal));
        verify(dealRepository).findByDealId(anyString());
        verify(validationService).validateDeal(any(FixedIncomeDerivativeDeal.class));
        verify(transformationService).transform(any(FixedIncomeDerivativeDeal.class));
        verifyNoMoreInteractions(dealRepository, jmsTemplate);
    }

    @Test
    void getDealById_ValidId_ReturnsDeal() {
        // Setup
        UUID id = UUID.randomUUID();
        when(dealRepository.findById(id)).thenReturn(Optional.of(testDeal));

        // Execute
        FixedIncomeDerivativeDeal result = dealService.getDealById(id);

        // Verify
        assertNotNull(result);
        assertEquals(testDeal.getDealId(), result.getDealId());
        verify(dealRepository).findById(id);
    }

    @Test
    void getDealById_InvalidId_ThrowsException() {
        // Setup
        UUID id = UUID.randomUUID();
        when(dealRepository.findById(id)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(DealProcessingException.class, () -> dealService.getDealById(id));
        verify(dealRepository).findById(id);
    }

    private FixedIncomeDerivativeDeal createTestDeal() {
        FixedIncomeDerivativeDeal deal = new FixedIncomeDerivativeDeal();
        deal.setDealId("TEST-DEAL-001");
        deal.setDealType("SWAP");
        deal.setExecutionVenue("OTC");
        deal.setStatus("NEW");
        deal.setProcessedAt(LocalDateTime.now());
        return deal;
    }
}