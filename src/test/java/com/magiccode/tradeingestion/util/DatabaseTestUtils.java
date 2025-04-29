package com.magiccode.tradeingestion.util;

import org.flywaydb.core.Flyway;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DatabaseTestUtils {

    @PersistenceContext
    private EntityManager entityManager;

    private final JdbcTemplate jdbcTemplate;
    private final Flyway flyway;

    public DatabaseTestUtils(JdbcTemplate jdbcTemplate, Flyway flyway) {
        this.jdbcTemplate = jdbcTemplate;
        this.flyway = flyway;
    }

    @Transactional
    public void cleanAndMigrate() {
        flyway.clean();
        flyway.migrate();
    }

    @Transactional
    public void clearAllTables() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate,
            "external_system_refs",
            "book",
            "booking_info",
            "deal_leg",
            "trader_info",
            "counterparty_info",
            "fixed_income_derivative_deal"
        );
    }

    @Transactional
    public void resetSequences() {
        List<String> sequences = jdbcTemplate.queryForList(
            "SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema = 'public'",
            String.class
        );
        
        sequences.forEach(sequence -> 
            jdbcTemplate.execute("ALTER SEQUENCE " + sequence + " RESTART WITH 1")
        );
    }

    @Transactional
    public void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    public Map<String, Object> getTableStats(String tableName) {
        return jdbcTemplate.queryForMap(
            "SELECT " +
            "COUNT(*) as row_count, " +
            "pg_size_pretty(pg_total_relation_size(?)) as total_size, " +
            "pg_size_pretty(pg_relation_size(?)) as table_size, " +
            "pg_size_pretty(pg_total_relation_size(?) - pg_relation_size(?)) as index_size",
            tableName, tableName, tableName, tableName
        );
    }

    public List<String> getActiveDeals() {
        return jdbcTemplate.queryForList(
            "SELECT deal_id FROM active_deals",
            String.class
        );
    }

    public Map<String, Long> getDealsByCurrency() {
        return jdbcTemplate.query(
            "SELECT leg_currency, COUNT(*) as count " +
            "FROM deals_by_currency " +
            "GROUP BY leg_currency",
            (rs, rowNum) -> Map.entry(
                rs.getString("leg_currency"),
                rs.getLong("count")
            )
        ).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
} 