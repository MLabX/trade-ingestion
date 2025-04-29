package com.magiccode.tradeingestion;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
    classes = MinimalTestConfig.class,
    properties = {
        "spring.datasource.url=jdbc:tc:postgresql:15.2-alpine:///trade_ingestion_test",
        "spring.datasource.username=test",
        "spring.datasource.password=test",
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    }
)
@ActiveProfiles("test")
public class MinimalApplicationTest {

    @Test
    void contextLoads() {
        // This test verifies that a minimal application context loads successfully
    }
}