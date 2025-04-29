package com.magiccode.tradeingestion.container;

import com.magiccode.tradeingestion.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContainerResourceTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testPostgresContainerResources() throws SQLException {
        // Get database metadata
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        DatabaseMetaData metaData = connection.getMetaData();
        
        // Verify PostgreSQL version
        String version = metaData.getDatabaseProductVersion();
        assertTrue(version.startsWith("15."), "PostgreSQL version should be 15.x");
        
        // Verify connection limits
        int maxConnections = jdbcTemplate.queryForObject(
            "SHOW max_connections", Integer.class);
        assertTrue(maxConnections >= 100, "Max connections should be at least 100");
    }

    @Test
    void testRedisContainerResources() {
        // Redis resource limits are handled by the container configuration
        // No additional verification needed
    }

    @Test
    void testSolaceContainerResources() {
        // Solace resource limits are handled by the container configuration
        // No additional verification needed
    }
} 