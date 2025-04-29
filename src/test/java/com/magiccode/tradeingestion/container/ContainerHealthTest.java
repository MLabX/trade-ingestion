package com.magiccode.tradeingestion.container;

import com.magiccode.tradeingestion.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContainerHealthTest extends BaseIntegrationTest {

    @Autowired
    private HealthEndpoint healthEndpoint;

    @Test
    void testPostgresContainerHealth() {
        // Verify PostgreSQL health
        assertEquals(Status.UP, healthEndpoint.health().getStatus());
    }

    @Test
    void testRedisContainerHealth() {
        // Verify Redis health
        assertEquals(Status.UP, healthEndpoint.health().getStatus());
    }

    @Test
    void testSolaceContainerHealth() {
        // Verify Solace health
        assertEquals(Status.UP, healthEndpoint.health().getStatus());
    }
} 