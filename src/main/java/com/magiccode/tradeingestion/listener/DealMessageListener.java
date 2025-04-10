package com.magiccode.tradeingestion.listener;

import com.magiccode.tradeingestion.exception.DealProcessingException;
import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.service.DealIngestionService;
import jakarta.jms.Message;
import jakarta.jms.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.support.JmsMessageHeaderAccessor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
public class DealMessageListener {
    private static final Logger logger = LoggerFactory.getLogger(DealMessageListener.class);

    private final DealIngestionService dealIngestionService;

    public DealMessageListener(DealIngestionService dealIngestionService) {
        this.dealIngestionService = dealIngestionService;
    }

    @JmsListener(destination = "deals", containerFactory = "jmsListenerContainerFactory")
    @Retryable(
        value = {DealProcessingException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void onMessage(Message message, Session session, JmsMessageHeaderAccessor headers) {
        try {
            logger.info("Received deal message: {}", message);

            // Process the deal
            Deal deal = message.getBody(Deal.class);
            if (deal == null) {
                throw new DealProcessingException("Received null deal message");
            }

            dealIngestionService.processDeal(deal);
            logger.info("Successfully processed deal: {}", deal);

        } catch (Exception e) {
            logger.error("Error processing deal message: {}", e.getMessage(), e);
            throw new DealProcessingException("Failed to process deal message", e);
        }
    }
} 