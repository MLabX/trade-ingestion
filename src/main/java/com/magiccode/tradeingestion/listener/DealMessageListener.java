package com.magiccode.tradeingestion.listener;

import com.magiccode.tradeingestion.exception.DealProcessingException;
import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.service.DealIngestionService;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.jms.Message;
import jakarta.jms.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.JmsMessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DealMessageListener {

    private final DealIngestionService dealService;
    private final JmsTemplate jmsTemplate;
    private static final String DLQ_DESTINATION = "deal.dlq";

    @JmsListener(destination = "deals", containerFactory = "jmsListenerContainerFactory")
    @Retry(name = "dealProcessing")
    public void onMessage(Message message, Session session, JmsMessageHeaderAccessor headers) {
        try {
            log.info("Received deal message");
            if (message == null) {
                throw new IllegalArgumentException("Message cannot be null");
            }
            
            Deal deal = message.getBody(Deal.class);
            if (deal == null) {
                throw new IllegalArgumentException("Deal cannot be null");
            }
            
            dealService.processDeal(deal);
            log.info("Successfully processed deal: {}", deal.getDealId());
        } catch (IllegalArgumentException e) {
            log.error("Invalid message received: {}", e.getMessage(), e);
            sendToDlq(message);
            throw e;
        } catch (DealProcessingException e) {
            log.error("Deal processing failed: {}", e.getMessage(), e);
            sendToDlq(message);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error processing deal message: {}", e.getMessage(), e);
            sendToDlq(message);
            throw new DealProcessingException("Failed to process deal message", e);
        }
    }

    private void sendToDlq(Message message) {
        try {
            jmsTemplate.convertAndSend(DLQ_DESTINATION, message);
            log.info("Message sent to DLQ: {}", DLQ_DESTINATION);
        } catch (Exception e) {
            log.error("Failed to send message to DLQ: {}", e.getMessage(), e);
        }
    }
}