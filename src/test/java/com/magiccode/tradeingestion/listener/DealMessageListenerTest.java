package com.magiccode.tradeingestion.listener;

import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.model.DealLeg;
import com.magiccode.tradeingestion.model.FixedIncomeDerivativeDeal;
import com.magiccode.tradeingestion.model.NotionalAmount;
import com.magiccode.tradeingestion.service.DealIngestionService;
import com.magiccode.tradeingestion.service.DealValidationService;
import com.magiccode.tradeingestion.service.DealTransformationService;
import com.magiccode.tradeingestion.exception.DealProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.JmsMessageHeaderAccessor;
import jakarta.jms.Message;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import java.util.List;
import java.time.LocalDate;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealMessageListenerTest {

    @Mock
    private DealIngestionService dealService;

    @Mock
    private DealValidationService validationService;

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private Message message;

    @Mock
    private TextMessage textMessage;

    @Mock
    private Session session;

    @Mock
    private JmsMessageHeaderAccessor headerAccessor;

    @InjectMocks
    private DealMessageListener listener;

    private static final String DLQ_DESTINATION = "deals.dlq";

    private FixedIncomeDerivativeDeal testDeal;

    @BeforeEach
    void setUp() {
        testDeal = createTestDeal();
    }

    @Test
    void whenNullMessage_thenThrowException() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> listener.onMessage(null, null, null));
        verifyNoInteractions(dealService, jmsTemplate);
    }

    @Test
    void whenValidMessage_thenProcessDeal() throws Exception {
        // Given
        when(message.getBody(Deal.class)).thenReturn(testDeal);
        when(dealService.processDeal(testDeal)).thenReturn(testDeal);

        // When
        listener.onMessage(message, null, null);

        // Then
        verify(dealService).processDeal(testDeal);
        verifyNoMoreInteractions(jmsTemplate);
    }

    @Test
    void whenInvalidMessage_thenSendToDlq() throws Exception {
        // Given
        when(message.getBody(Deal.class)).thenReturn(null);

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> listener.onMessage(message, null, null));
        verify(jmsTemplate).convertAndSend(eq(DLQ_DESTINATION), eq(message));
        verifyNoInteractions(dealService);
    }

    @Test
    void whenProcessingFails_thenSendToDlq() throws Exception {
        // Given
        when(message.getBody(Deal.class)).thenReturn(testDeal);
        when(dealService.processDeal(testDeal)).thenThrow(new DealProcessingException("Processing failed"));

        // When/Then
        assertThrows(DealProcessingException.class, () -> listener.onMessage(message, null, null));
        verify(dealService).processDeal(testDeal);
        verify(jmsTemplate).convertAndSend(eq(DLQ_DESTINATION), eq(message));
    }

    @Test
    void whenValidationFails_thenSendToDlq() throws Exception {
        // Given
        when(message.getBody(Deal.class)).thenReturn(testDeal);
        when(dealService.processDeal(testDeal)).thenThrow(new IllegalArgumentException("Validation failed"));

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> listener.onMessage(message, null, null));
        verify(dealService).processDeal(testDeal);
        verify(jmsTemplate).convertAndSend(eq(DLQ_DESTINATION), eq(message));
    }

    private FixedIncomeDerivativeDeal createTestDeal() {
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

        return FixedIncomeDerivativeDeal.builder()
            .dealId("TEST-123")
            .dealType("SWAP")
            .tradeDate(LocalDate.now())
            .valueDate(LocalDate.now().plusDays(2))
            .maturityDate(LocalDate.now().plusYears(1))
            .status("NEW")
            .clientId("TEST-CLIENT")
            .instrumentId("TEST-INSTRUMENT")
            .quantity(BigDecimal.valueOf(1000000))
            .price(BigDecimal.valueOf(1.0))
            .currency("USD")
            .legs(List.of(payLeg, receiveLeg))
            .build();
    }
} 