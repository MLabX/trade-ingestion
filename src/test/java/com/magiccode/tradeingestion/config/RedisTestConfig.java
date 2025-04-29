package com.magiccode.tradeingestion.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@TestConfiguration
@ActiveProfiles("redis")
public class RedisTestConfig extends BaseIntegrationTestConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(REDIS_CONTAINER.getHost());
        config.setPort(REDIS_CONTAINER.getFirstMappedPort());
        return new LettuceConnectionFactory(config);
    }
} 