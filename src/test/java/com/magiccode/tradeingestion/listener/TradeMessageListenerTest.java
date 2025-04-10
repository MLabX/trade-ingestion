package com.magiccode.tradeingestion.listener;

import com.magiccode.tradeingestion.exception.DealProcessingException;
import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.service.DealIngestionService;
import jakarta.jms.Message;
import jakarta.jms.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.support.JmsMessageHeaderAccessor;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealMessageListenerTest {

    @Mock
    private DealIngestionService dealIngestionService;

    @Mock
    private Message message;

    @Mock
    private Session session;

    @Mock
    private JmsMessageHeaderAccessor headers;

    private DealMessageListener dealMessageListener;

    @BeforeEach
    void setUp() {
        dealMessageListener = new DealMessageListener(dealIngestionService);
    }

    @Test
    void testOnMessage_Success() throws Exception {
        // Arrange
        Deal testDeal = Deal.createNew(
            "DEAL123",
            "CLIENT1",
            "INSTR1",
            new BigDecimal("100"),
            new BigDecimal("10.50"),
            "USD"
        );
        when(message.getBody(Deal.class)).thenReturn(testDeal);

        // Act
        dealMessageListener.onMessage(message, session, headers);

        // Assert
        verify(dealIngestionService).processDeal(testDeal);
    }

    @Test
    void testOnMessage_NullDeal() throws Exception {
        // Arrange
        when(message.getBody(Deal.class)).thenReturn(null);

        // Act & Assert
        assertThrows(DealProcessingException.class, () -> 
            dealMessageListener.onMessage(message, session, headers)
        );
        verify(dealIngestionService, never()).processDeal(any());
    }

    @Test
    void testOnMessage_ProcessingException() throws Exception {
        // Arrange
        Deal testDeal = Deal.createNew(
            "DEAL123",
            "CLIENT1",
            "INSTR1",
            new BigDecimal("100"),
            new BigDecimal("10.50"),
            "USD"
        );
        when(message.getBody(Deal.class)).thenReturn(testDeal);
        doThrow(new DealProcessingException("Test error"))
            .when(dealIngestionService).processDeal(any());

        // Act & Assert
        assertThrows(DealProcessingException.class, () -> 
            dealMessageListener.onMessage(message, session, headers)
        );
        verify(dealIngestionService).processDeal(testDeal);
    }
} 