package com.magiccode.tradeingestion.listener;

import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.model.FixedIncomeDerivativeDeal;
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
    void setUp() throws Exception {
        testDeal = new FixedIncomeDerivativeDeal();
        testDeal.setDealId("TEST-001");
        when(textMessage.getText()).thenReturn("{\"dealId\":\"TEST-001\"}");
    }

    @Test
    void whenValidMessage_thenProcessDeal() throws Exception {
        // Setup
        when(dealService.processDeal(any(Deal.class))).thenReturn(testDeal);
        when(validationService.validateDeal(any(Deal.class))).thenReturn(List.of());

        // Execute
        listener.onMessage(textMessage, session, headerAccessor);

        // Verify
        verify(dealService).processDeal(any(Deal.class));
        verify(validationService).validateDeal(any(Deal.class));
        verify(jmsTemplate, never()).convertAndSend(eq(DLQ_DESTINATION), any(String.class));
    }

    @Test
    void whenInvalidMessage_thenSendToDlq() throws Exception {
        // Arrange
        when(textMessage.getText()).thenReturn("invalid json");

        // Act
        listener.onMessage(textMessage, session, headerAccessor);

        // Assert
        verify(jmsTemplate).convertAndSend(eq(DLQ_DESTINATION), eq(textMessage));
        verifyNoInteractions(dealService, validationService);
    }

    @Test
    void whenProcessingFails_thenSendToDlq() throws Exception {
        // Setup
        when(dealService.processDeal(any(Deal.class))).thenThrow(new DealProcessingException("Processing failed"));
        when(validationService.validateDeal(any(Deal.class))).thenReturn(List.of());

        // Execute
        listener.onMessage(textMessage, session, headerAccessor);

        // Verify
        verify(dealService).processDeal(any(Deal.class));
        verify(validationService).validateDeal(any(Deal.class));
        verify(jmsTemplate).convertAndSend(eq(DLQ_DESTINATION), eq(textMessage));
    }

    @Test
    void whenValidationFails_thenSendToDlq() throws Exception {
        // Setup
        when(validationService.validateDeal(any(Deal.class)))
            .thenReturn(List.of("Invalid deal: missing required fields"));

        // Execute
        listener.onMessage(textMessage, session, headerAccessor);

        // Verify
        verify(validationService).validateDeal(any(Deal.class));
        verify(dealService, never()).processDeal(any(Deal.class));
        verify(jmsTemplate).convertAndSend(eq(DLQ_DESTINATION), eq(textMessage));
    }

    @Test
    void whenNullMessage_thenThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> listener.onMessage(null, session, headerAccessor));
        verifyNoMoreInteractions(dealService, jmsTemplate, validationService);
    }
} 