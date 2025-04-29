package com.magiccode.tradeingestion.integration.e2e;

import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.model.FixedIncomeDerivativeDeal;
import com.magiccode.tradeingestion.repository.DealRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class EndToEndFlowTest {

    private static final Network network = Network.newNetwork();

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withNetwork(network)
            .withNetworkAliases("postgres")
            .withDatabaseName("deals")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    private static final GenericContainer<?> solace = new GenericContainer<>("solace/solace-pubsub-standard:latest")
            .withNetwork(network)
            .withNetworkAliases("solace")
            .withEnv("username_admin_globalaccesslevel", "admin")
            .withEnv("username_admin_password", "admin")
            .withEnv("system_scaling_maxconnectioncount", "100")
            .withEnv("service_semp_plaintext_port", "8080")
            .withEnv("service_smf_port", "55555")
            .withExposedPorts(8080, 55555, 55003, 55443, 5672, 1883, 8000, 8443, 943, 80, 443, 5671, 1884, 8008, 1443, 9443)
            .waitingFor(Wait.forLogMessage(".*Solace Message Router is ready for client connections.*", 1))
            .withStartupTimeout(java.time.Duration.ofMinutes(2));

    @Container
    private static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withNetwork(network)
            .withNetworkAliases("redis")
            .withExposedPorts(6379);

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private DealRepository dealRepository;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL properties
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Solace properties
        registry.add("solace.host", () -> solace.getHost());
        registry.add("solace.port", () -> solace.getMappedPort(8080));
        registry.add("solace.username", () -> "admin");
        registry.add("solace.password", () -> "admin");
        registry.add("solace.vpn", () -> "default");

        // Redis properties
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }

    @BeforeAll
    static void setup() {
        postgres.start();
        solace.start();
        redis.start();
    }

    @AfterAll
    static void cleanup() {
        postgres.stop();
        solace.stop();
        redis.stop();
    }

    @Test
    void testEndToEndFlow() {
        // Create a test deal
        FixedIncomeDerivativeDeal deal = FixedIncomeDerivativeDeal.builder()
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

        // Send the deal through the system
        jmsTemplate.convertAndSend("deals.inbound", deal);

        // Wait for the deal to be processed and persisted
        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            // Check if the deal was persisted
            Optional<Deal> persistedDeal = dealRepository.findByDealId(deal.getDealId());
            assertTrue(persistedDeal.isPresent(), "Deal should be persisted");
            assertEquals("PROCESSED", persistedDeal.get().getStatus(), "Deal should be processed");

            // Check if the processed deal was sent to the output queue
            Deal processedDeal = (Deal) jmsTemplate.receiveAndConvert("deals.outbound");
            assertNotNull(processedDeal, "Processed deal should be sent to output queue");
            assertEquals(deal.getDealId(), processedDeal.getDealId(), "Deal IDs should match");
            assertEquals("PROCESSED", processedDeal.getStatus(), "Deal should be processed");
        });
    }

    @Test
    void testIdempotency() {
        // Create a test deal
        FixedIncomeDerivativeDeal deal = FixedIncomeDerivativeDeal.builder()
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

        // Send the same deal twice
        jmsTemplate.convertAndSend("deals.inbound", deal);
        jmsTemplate.convertAndSend("deals.inbound", deal);

        // Wait for processing
        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            // Check if the deal was persisted only once
            Optional<Deal> persistedDeal = dealRepository.findByDealId(deal.getDealId());
            assertTrue(persistedDeal.isPresent(), "Deal should be persisted");
            assertEquals(1L, persistedDeal.get().getVersion(), "Deal should be processed only once");

            // Check if the processed deal was sent to the output queue only once
            Deal processedDeal = (Deal) jmsTemplate.receiveAndConvert("deals.outbound");
            assertNotNull(processedDeal, "Processed deal should be sent to output queue");
            assertNull(jmsTemplate.receiveAndConvert("deals.outbound"), "No duplicate messages should be sent");
        });
    }
} 