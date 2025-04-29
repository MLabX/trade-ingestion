package com.magiccode.tradeingestion;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:15.2-alpine:///trade_ingestion_test",
    "spring.datasource.username=test",
    "spring.datasource.password=test",
    "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("test")
class DealIngestionApplicationTests {

    @Test
    void contextLoads() {
        // This test verifies that the application context loads successfully
    }
} 
