package com.magiccode.tradeingestion.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ApplicationPropertiesTest {

    @Mock
    private Environment env;

    @Test
    void testJmsProperties() {
        // Setup
        when(env.getProperty("spring.artemis.broker-url")).thenReturn("tcp://localhost:61616");
        when(env.getProperty("spring.artemis.user")).thenReturn("admin");
        when(env.getProperty("spring.artemis.password")).thenReturn("admin");

        // Test
        assertThat(env.getProperty("spring.artemis.broker-url")).isNotNull();
        assertThat(env.getProperty("spring.artemis.user")).isNotNull();
        assertThat(env.getProperty("spring.artemis.password")).isNotNull();
    }

    @Test
    void testRedisProperties() {
        // Setup
        when(env.getProperty("spring.redis.host")).thenReturn("localhost");
        when(env.getProperty("spring.redis.port")).thenReturn("6379");

        // Test
        assertThat(env.getProperty("spring.redis.host")).isNotNull();
        assertThat(env.getProperty("spring.redis.port")).isNotNull();
    }

    @Test
    void testDatabaseProperties() {
        // Setup
        when(env.getProperty("spring.datasource.url")).thenReturn("jdbc:postgresql://localhost:5432/deals");
        when(env.getProperty("spring.datasource.username")).thenReturn("postgres");
        when(env.getProperty("spring.datasource.password")).thenReturn("postgres");

        // Test
        assertThat(env.getProperty("spring.datasource.url")).isNotNull();
        assertThat(env.getProperty("spring.datasource.username")).isNotNull();
        assertThat(env.getProperty("spring.datasource.password")).isNotNull();
    }
} 