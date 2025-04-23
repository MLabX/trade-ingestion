package com.magiccode.tradeingestion.listener;

import com.magiccode.tradeingestion.model.FixedIncomeDerivativeDeal;
import com.magiccode.tradeingestion.service.FixedIncomeDerivativeDealService;
import com.magiccode.tradeingestion.exception.DealProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import jakarta.jms.Message;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FixedIncomeDerivativeDealListenerTest {

    @Mock
    private FixedIncomeDerivativeDealService dealService;

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private TextMessage message;

    @Mock
    private Session session;

    @InjectMocks
    private FixedIncomeDerivativeDealListener listener;

    private FixedIncomeDerivativeDeal testDeal;
    private static final String DLQ_DESTINATION = "fixed-income-deals.dlq";

    @BeforeEach
    void setUp() {
        testDeal = FixedIncomeDerivativeDeal.builder()
            .dealId("TEST-001")
            .quantity(new BigDecimal("100"))
            .price(new BigDecimal("100.0"))
            .build();
    }

    @Test
    void testOnMessage_Success() throws Exception {
        // Arrange
        String messageContent = "{\"dealId\":\"TEST-001\",\"quantity\":100,\"price\":100.0}";
        when(message.getText()).thenReturn(messageContent);
        when(dealService.processDeal(any(FixedIncomeDerivativeDeal.class))).thenReturn(testDeal);

        // Act
        listener.onMessage(message, session);

        // Assert
        verify(dealService).processDeal(any(FixedIncomeDerivativeDeal.class));
        verifyNoInteractions(jmsTemplate);
    }

    @Test
    void testOnMessage_InvalidMessageType() throws Exception {
        // Arrange
        String invalidMessageContent = "invalid json";
        when(message.getText()).thenReturn(invalidMessageContent);

        // Act
        listener.onMessage(message, session);

        // Assert
        verify(jmsTemplate).convertAndSend(eq(DLQ_DESTINATION), eq(message));
        verifyNoInteractions(dealService);
    }

    @Test
    void testOnMessage_NullDeal() throws Exception {
        // Arrange
        when(message.getText()).thenReturn(null);

        // Act
        listener.onMessage(message, session);

        // Assert
        verify(jmsTemplate).convertAndSend(eq(DLQ_DESTINATION), eq(message));
        verifyNoInteractions(dealService);
    }

    @Test
    void testOnMessage_ProcessingError() throws Exception {
        // Arrange
        String messageContent = "{\"dealId\":\"TEST-001\",\"quantity\":100,\"price\":100.0}";
        when(message.getText()).thenReturn(messageContent);
        when(dealService.processDeal(any(FixedIncomeDerivativeDeal.class)))
            .thenThrow(new DealProcessingException("Processing failed"));

        // Act
        listener.onMessage(message, session);

        // Assert
        verify(dealService).processDeal(any(FixedIncomeDerivativeDeal.class));
        verify(jmsTemplate).convertAndSend(eq(DLQ_DESTINATION), eq(message));
    }
}