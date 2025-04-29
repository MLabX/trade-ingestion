package com.magiccode.tradeingestion;

import com.magiccode.tradeingestion.config.SolaceContainerConfig;
import com.magiccode.tradeingestion.config.SolaceProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:15.2-alpine:///trade_ingestion_test",
    "spring.datasource.username=test",
    "spring.datasource.password=test",
    "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Testcontainers
@Import(SolaceContainerConfig.class)
public class ContainerHealthCheckTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:15.2-alpine")
        .withDatabaseName("trade_ingestion_test")
        .withUsername("test")
        .withPassword("test")
        .withStartupTimeout(Duration.ofMinutes(2));

    @Container
    private static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("redis:6.2.6"))
        .withExposedPorts(6379)
        .withStartupTimeout(Duration.ofMinutes(2))
        .waitingFor(Wait.forListeningPort());

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("solaceContainer")
    private GenericContainer<?> SOLACE_CONTAINER;

    @Autowired
    private SolaceProperties solaceProperties;

    @Test
    void testContainerStartup() {
        // Test will pass if all containers start successfully
        System.out.println("PostgreSQL container is running: " + POSTGRES_CONTAINER.isRunning());
        System.out.println("Redis container is running: " + REDIS_CONTAINER.isRunning());
        System.out.println("Solace container is running: " + SOLACE_CONTAINER.isRunning());

        if (POSTGRES_CONTAINER.isRunning()) {
            System.out.println("PostgreSQL port: " + POSTGRES_CONTAINER.getFirstMappedPort());
        }

        if (REDIS_CONTAINER.isRunning()) {
            System.out.println("Redis port: " + REDIS_CONTAINER.getFirstMappedPort());
        }

        if (SOLACE_CONTAINER.isRunning()) {
            System.out.println("Solace ports:");
            for (int port : solaceProperties.getRequiredPorts()) {
                System.out.println("  Port " + port + ": " + SOLACE_CONTAINER.getMappedPort(port));
            }

            // Print Solace container logs
            System.out.println("\nSolace container logs:");
            System.out.println(SOLACE_CONTAINER.getLogs());
        }
    }
} 
