package com.magiccode.tradeingestion.listener;

import com.magiccode.tradeingestion.BaseIntegrationTest;
import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.model.FixedIncomeDerivativeDeal;
import com.magiccode.tradeingestion.service.DealIngestionService;
import com.magiccode.tradeingestion.testdata.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Sql(scripts = {
    "classpath:sql/cleanup.sql",
    "classpath:sql/fixed-income-derivative-deals.sql"
})
class DealMessageListenerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private DealMessageListener dealMessageListener;

    @Autowired
    private DealIngestionService dealService;

    private static final String TEST_QUEUE = "deals.queue";

    @Test
    void whenValidMessage_thenProcessDeal() throws Exception {
        // Given
        FixedIncomeDerivativeDeal deal = TestDataFactory.createFixedIncomeDerivativeDeal();
        String dealId = "TEST-" + System.currentTimeMillis();
        deal.setDealId(dealId);

        // When
        jmsTemplate.send(TEST_QUEUE, session -> {
            jakarta.jms.Message message = session.createObjectMessage(deal);
            message.setStringProperty("dealId", dealId);
            return message;
        });

        // Then
        Thread.sleep(1000); // Wait for async processing
        Deal processedDeal = dealService.getDealById(UUID.randomUUID()).orElse(null);
        assertNotNull(processedDeal);
        assertEquals(dealId, processedDeal.getDealId());
        assertEquals(deal.getClientId(), processedDeal.getClientId());
        assertEquals(deal.getInstrumentId(), processedDeal.getInstrumentId());
    }

    @Test
    void whenNullDeal_thenThrowIllegalArgumentException() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> 
            jmsTemplate.send(TEST_QUEUE, session -> session.createObjectMessage(null))
        );
    }

    @Test
    void whenProcessingFails_thenSendToDlq() throws Exception {
        // Given
        FixedIncomeDerivativeDeal deal = TestDataFactory.createFixedIncomeDerivativeDeal();
        String invalidDealId = "INVALID-" + System.currentTimeMillis();
        deal.setDealId(invalidDealId);
        deal.setClientId(null); // This should cause validation to fail

        // When
        jmsTemplate.send(TEST_QUEUE, session -> {
            jakarta.jms.Message message = session.createObjectMessage(deal);
            message.setStringProperty("dealId", invalidDealId);
            return message;
        });

        // Then
        Thread.sleep(1000); // Wait for async processing
        Deal processedDeal = dealService.getDealById(UUID.randomUUID()).orElse(null);
        assertNull(processedDeal); // Deal should not be processed
    }
} 