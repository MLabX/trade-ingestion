package com.magiccode.tradeingestion.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationPropertiesTest {

    @Autowired
    private Environment env;

    @Test
    void testJmsProperties() {
        assertThat(env.getProperty("spring.artemis.broker-url")).isNotNull();
        assertThat(env.getProperty("spring.artemis.user")).isNotNull();
        assertThat(env.getProperty("spring.artemis.password")).isNotNull();
    }

    @Test
    void testRedisProperties() {
        assertThat(env.getProperty("spring.redis.host")).isNotNull();
        assertThat(env.getProperty("spring.redis.port")).isNotNull();
    }

    @Test
    void testDatabaseProperties() {
        assertThat(env.getProperty("spring.datasource.url")).isNotNull();
        assertThat(env.getProperty("spring.datasource.username")).isNotNull();
        assertThat(env.getProperty("spring.datasource.password")).isNotNull();
    }
} 