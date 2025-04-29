package com.magiccode.tradeingestion.integration;

import com.magiccode.tradeingestion.config.TestContainerConfig;
import com.magiccode.tradeingestion.model.TestDeal;
import com.magiccode.tradeingestion.service.DealIngestionService;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;
import jakarta.jms.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class SolaceIntegrationTest extends TestContainerConfig {

    @Container
    private static final GenericContainer<?> solace = new GenericContainer<>("solace/solace-pubsub-standard:latest")
            .withExposedPorts(8080, 55555, 55003, 55443, 5672, 1883, 8000, 8443, 943, 80, 443, 5671, 1884, 8008, 1443, 9443)
            .withStartupTimeout(java.time.Duration.ofMinutes(2));

    @Autowired
    private DealIngestionService dealService;

    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private MessageConsumer consumer;

    @BeforeEach
    void setUp() throws Exception {
        SolConnectionFactory connectionFactory = SolJmsUtility.createConnectionFactory();
        connectionFactory.setHost(solace.getHost());
        connectionFactory.setVPN("default");
        connectionFactory.setUsername("admin");
        connectionFactory.setPassword("admin");
        connectionFactory.setPort(solace.getMappedPort(55555));

        // Cast the connection to jakarta.jms.Connection
        connection = (Connection) connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic("deals");
        producer = session.createProducer(topic);
        consumer = session.createConsumer(topic);
        connection.start();
    }

    @Test
    void testDealProcessing() throws Exception {
        // Create a test deal
        TestDeal deal = TestDeal.builder()
                .dealId(UUID.randomUUID().toString())
                .clientId("TEST_CLIENT")
                .instrumentId("TEST_INSTRUMENT")
                .quantity(BigDecimal.ONE)
                .price(BigDecimal.TEN)
                .currency("USD")
                .status("NEW")
                .version(1L)
                .dealDate(LocalDateTime.now())
                .build();

        // Send the deal to Solace
        ObjectMessage message = session.createObjectMessage(deal);
        producer.send(message);

        // Wait for the message to be processed
        Message receivedMessage = consumer.receive(5000);
        assertNotNull(receivedMessage, "No message received within timeout");
        assertNotNull(receivedMessage.getJMSMessageID(), "Message ID should not be null");

        // Verify the deal was processed
        TestDeal processedDeal = (TestDeal) dealService.getDealById(deal.getId()).orElse(null);
        assertNotNull(processedDeal, "Deal should be processed and stored");
        assertEquals(deal.getDealId(), processedDeal.getDealId());
        assertEquals(deal.getClientId(), processedDeal.getClientId());
        assertEquals(deal.getInstrumentId(), processedDeal.getInstrumentId());
        assertEquals(deal.getQuantity(), processedDeal.getQuantity());
        assertEquals(deal.getPrice(), processedDeal.getPrice());
        assertEquals(deal.getCurrency(), processedDeal.getCurrency());
    }
} 