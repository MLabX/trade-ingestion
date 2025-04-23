package com.magiccode.tradeingestion;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:15.2-alpine");

    @Container
    private static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("redis:6.2.6"))
        .withExposedPorts(6379);

    @Container
    protected static final GenericContainer<?> solaceContainer = new GenericContainer<>("solace/solace-pubsub-standard:latest")
        .withExposedPorts(8080, 55555, 55003, 55443)
        .withEnv("username_admin_globalaccesslevel", "admin")
        .withEnv("username_admin_password", "admin")
        .withEnv("system_scaling_maxconnectioncount", "100");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);

        // Solace
        registry.add("solace.jms.host", solaceContainer::getHost);
        registry.add("solace.jms.port", () -> solaceContainer.getMappedPort(55555));
        registry.add("solace.jms.msgVpn", () -> "default");
        registry.add("solace.jms.clientUsername", () -> "default");
        registry.add("solace.jms.clientPassword", () -> "default");
    }

    @BeforeAll
    static void setUp() {
        POSTGRES_CONTAINER.start();
        REDIS_CONTAINER.start();
    }
} 