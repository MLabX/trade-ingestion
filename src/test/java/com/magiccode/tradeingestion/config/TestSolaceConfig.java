package com.magiccode.tradeingestion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.GenericContainer;
import org.mockito.Mockito;

@Configuration
@Profile("test")
public class TestSolaceConfig {

    @Bean
    @Primary
    public GenericContainer<?> solaceContainer() {
        // Return a mock container for tests that don't need a real Solace container
        return Mockito.mock(GenericContainer.class);
    }

    @Bean
    @Primary
    public SolaceContainerManager solaceContainerManager() {
        // Return a mock SolaceContainerManager for tests that don't need a real one
        return Mockito.mock(SolaceContainerManager.class);
    }
}