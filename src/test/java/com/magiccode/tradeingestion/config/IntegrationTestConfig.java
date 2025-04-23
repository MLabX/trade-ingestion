package com.magiccode.tradeingestion.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class IntegrationTestConfig {

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private static final GenericContainer<?> solace = new GenericContainer<>(DockerImageName.parse("solace/solace-pubsub-standard:latest"))
            .withExposedPorts(55555, 8080, 8008)
            .withEnv("username_admin_globalaccesslevel", "admin")
            .withEnv("username_admin_password", "admin")
            .withEnv("system_scaling_maxconnectioncount", "100");

    static {
        postgres.start();
        solace.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL properties
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Solace properties
        registry.add("solace.jms.host", () -> solace.getHost());
        registry.add("solace.jms.port", () -> solace.getMappedPort(55555));
        registry.add("solace.jms.username", () -> "admin");
        registry.add("solace.jms.password", () -> "admin");
        registry.add("solace.jms.vpn", () -> "default");
    }

    @Bean
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return postgres;
    }

    @Bean
    public GenericContainer<?> solaceContainer() {
        return solace;
    }
} 