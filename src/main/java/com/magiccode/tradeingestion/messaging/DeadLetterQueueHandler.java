package com.magiccode.tradeingestion.messaging;

import com.magiccode.tradeingestion.exception.DealProcessingException;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class DeadLetterQueueHandler {
    private static final Logger logger = LoggerFactory.getLogger(DeadLetterQueueHandler.class);
    private static final String DLQ_DESTINATION = "DEAL.DLQ";
    
    private final JmsTemplate jmsTemplate;
    
    public DeadLetterQueueHandler(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }
    
    public void handleFailedMessage(Message originalMessage, Exception exception) {
        try {
            String errorMessage = exception.getMessage();
            String originalContent = extractMessageContent(originalMessage);
            String errorId = UUID.randomUUID().toString();
            
            // Create a new message with error details
            jmsTemplate.send(DLQ_DESTINATION, session -> {
                TextMessage dlqMessage = session.createTextMessage();
                dlqMessage.setText(originalContent);
                dlqMessage.setStringProperty("ERROR_ID", errorId);
                dlqMessage.setStringProperty("ERROR_MESSAGE", errorMessage);
                dlqMessage.setStringProperty("ERROR_TIME", LocalDateTime.now().toString());
                dlqMessage.setStringProperty("ORIGINAL_DESTINATION", originalMessage.getJMSDestination().toString());
                
                if (exception instanceof DealProcessingException) {
                    dlqMessage.setStringProperty("ERROR_TYPE", "DEAL_PROCESSING");
                } else {
                    dlqMessage.setStringProperty("ERROR_TYPE", "SYSTEM");
                }
                
                return dlqMessage;
            });
            
            logger.info("Message sent to DLQ with error ID: {}", errorId);
        } catch (Exception e) {
            logger.error("Failed to send message to DLQ", e);
        }
    }
    
    private String extractMessageContent(Message message) {
        try {
            if (message instanceof TextMessage textMessage) {
                return textMessage.getText();
            } else {
                return "Non-text message: " + message.getClass().getName();
            }
        } catch (JMSException e) {
            logger.error("Failed to extract message content", e);
            return "Error extracting message content";
        }
    }
} 