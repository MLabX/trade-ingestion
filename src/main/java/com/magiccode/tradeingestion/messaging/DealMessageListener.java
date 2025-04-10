package com.magiccode.tradeingestion.messaging;

import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.service.DealIngestionService;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.MapMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class DealMessageListener implements MessageListener {
    
    private static final Logger logger = LoggerFactory.getLogger(DealMessageListener.class);
    
    @Autowired
    private DealIngestionService dealIngestionService;
    
    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof MapMessage mapMessage) {
                String symbol = mapMessage.getString("symbol");
                double amount = mapMessage.getDouble("amount");
                int quantity = mapMessage.getInt("quantity");
                String side = mapMessage.getString("side");
                
                // Create a Deal object from the message
                Deal deal = new Deal(
                    UUID.randomUUID(),
                    "DEAL-" + System.currentTimeMillis(),
                    "CLIENT-" + System.currentTimeMillis(),
                    symbol,
                    new BigDecimal(quantity),
                    new BigDecimal(amount),
                    "USD",
                    "NEW",
                    1L,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    null
                );
                
                dealIngestionService.processDeal(deal);
                
                logger.info("Processed deal message: symbol={}, amount={}, quantity={}, side={}", 
                    symbol, amount, quantity, side);
            } else {
                logger.warn("Received message is not a MapMessage: {}", message.getClass().getName());
            }
        } catch (JMSException e) {
            logger.error("Error processing message: {}", e.getMessage(), e);
        }
    }
} 