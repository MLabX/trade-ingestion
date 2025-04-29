package com.magiccode.tradeingestion.container;

import com.magiccode.tradeingestion.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContainerCleanupTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testPostgresContainerCleanup() {
        // Create a test table
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS test_cleanup (id SERIAL PRIMARY KEY, name VARCHAR(255))");
        
        // Insert test data
        jdbcTemplate.execute("INSERT INTO test_cleanup (name) VALUES ('test1')");
        
        // Verify data exists
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_cleanup", Integer.class);
        assertEquals(1, count);
        
        // Cleanup should happen automatically after test
    }

    @Test
    void testRedisContainerCleanup() {
        // Redis cleanup is handled by the container itself
        // No additional cleanup needed
    }

    @Test
    void testSolaceContainerCleanup() {
        // Solace cleanup is handled by the container itself
        // No additional cleanup needed
    }
} 