package com.magiccode.tradeingestion.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@TestConfiguration
@ActiveProfiles("integration")
public class BaseIntegrationTestConfig {

    protected static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:15.2-alpine")
        .withStartupTimeout(Duration.ofMinutes(2));

    protected static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("redis:6.2.6"))
        .withExposedPorts(6379)
        .withStartupTimeout(Duration.ofMinutes(2));

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // Start containers in order
        POSTGRES_CONTAINER.start();
        REDIS_CONTAINER.start();

        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);
    }

    @Bean
    public PostgreSQLContainer<?> postgresContainer() {
        return POSTGRES_CONTAINER;
    }

    @Bean
    public GenericContainer<?> redisContainer() {
        return REDIS_CONTAINER;
    }
} 