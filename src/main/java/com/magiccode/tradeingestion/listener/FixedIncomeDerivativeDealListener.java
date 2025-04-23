package com.magiccode.tradeingestion.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magiccode.tradeingestion.exception.DealProcessingException;
import com.magiccode.tradeingestion.model.FixedIncomeDerivativeDeal;
import com.magiccode.tradeingestion.service.FixedIncomeDerivativeDealService;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.jms.Message;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FixedIncomeDerivativeDealListener {

    private final FixedIncomeDerivativeDealService dealService;
    private final ObjectMapper objectMapper;

    @JmsListener(destination = "fixed.income.derivative.deals")
    @Retry(name = "fixedIncomeDerivativeDealProcessing")
    public void onMessage(Message message, Session session) {
        try {
            if (message instanceof TextMessage) {
                String messageText = ((TextMessage) message).getText();
                FixedIncomeDerivativeDeal deal = objectMapper.readValue(messageText, FixedIncomeDerivativeDeal.class);
                log.info("Received fixed income derivative deal: {}", deal.getDealId());
                dealService.processDeal(deal);
                log.info("Successfully processed fixed income derivative deal: {}", deal.getDealId());
            } else {
                log.error("Received unsupported message type: {}", message.getClass().getName());
                throw new DealProcessingException("Unsupported message type: " + message.getClass().getName());
            }
        } catch (Exception e) {
            log.error("Error processing fixed income derivative deal: {}", e.getMessage(), e);
            throw new DealProcessingException("Failed to process deal", e);
        }
    }
} 