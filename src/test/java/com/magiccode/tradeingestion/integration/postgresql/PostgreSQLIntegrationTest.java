package com.magiccode.tradeingestion.integration.postgresql;

import com.magiccode.tradeingestion.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;

class PostgreSQLIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testDatabaseConnection() {
        // Given
        String testQuery = "SELECT 1";

        // When
        Integer result = jdbcTemplate.queryForObject(testQuery, Integer.class);

        // Then
        assertEquals(1, result);
    }
} 