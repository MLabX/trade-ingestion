package com.magiccode.tradeingestion.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationValidationServiceTest {

    @Mock
    private Environment environment;

    @Mock
    private SolaceProperties solaceProperties;

    private ConfigurationValidationService validationService;

    @BeforeEach
    void setUp() {
        // Mock required environment variables with lenient stubbings
        lenient().when(environment.getProperty("spring.profiles.active")).thenReturn("test");
        
        // Mock Solace configuration
        lenient().when(solaceProperties.getRequiredPorts()).thenReturn(Arrays.asList(55555, 55556));
        lenient().doNothing().when(solaceProperties).validate();
        
        validationService = new ConfigurationValidationService(environment, solaceProperties);
    }

    @Test
    void validateConfiguration_ValidConfig_Success() {
        // Setup
        lenient().when(environment.getProperty("spring.task.execution.pool.core-size")).thenReturn("2");
        lenient().when(environment.getProperty("spring.task.execution.pool.max-size")).thenReturn("4");
        lenient().when(environment.getProperty("spring.datasource.hikari.minimum-idle")).thenReturn("2");
        lenient().when(environment.getProperty("spring.datasource.hikari.maximum-pool-size")).thenReturn("4");
        lenient().when(environment.getProperty("server.ssl.enabled")).thenReturn("false");
        lenient().when(environment.getProperty("spring.security.authentication.type")).thenReturn("basic");

        // Execute
        validationService.validateConfiguration();

        // Verify
        assertTrue(validationService.isValidationComplete());
    }

    @Test
    void validateConfiguration_InvalidThreadPoolConfig_ThrowsException() {
        // Setup
        lenient().when(environment.getProperty("spring.task.execution.pool.core-size")).thenReturn("4");
        lenient().when(environment.getProperty("spring.task.execution.pool.max-size")).thenReturn("2");
        lenient().when(environment.getProperty("spring.datasource.hikari.minimum-idle")).thenReturn("2");
        lenient().when(environment.getProperty("spring.datasource.hikari.maximum-pool-size")).thenReturn("4");
        lenient().when(environment.getProperty("server.ssl.enabled")).thenReturn("false");

        // Execute and Verify
        assertThrows(IllegalStateException.class, () -> validationService.validateConfiguration());
        assertFalse(validationService.isValidationComplete());
    }

    @Test
    void validateConfiguration_InvalidConnectionPoolConfig_ThrowsException() {
        // Setup
        lenient().when(environment.getProperty("spring.task.execution.pool.core-size")).thenReturn("2");
        lenient().when(environment.getProperty("spring.task.execution.pool.max-size")).thenReturn("4");
        lenient().when(environment.getProperty("spring.datasource.hikari.minimum-idle")).thenReturn("4");
        lenient().when(environment.getProperty("spring.datasource.hikari.maximum-pool-size")).thenReturn("2");
        lenient().when(environment.getProperty("server.ssl.enabled")).thenReturn("false");

        // Execute and Verify
        assertThrows(IllegalStateException.class, () -> validationService.validateConfiguration());
        assertFalse(validationService.isValidationComplete());
    }

    @Test
    void validateConfiguration_InvalidSecurityConfig_ThrowsException() {
        // Setup
        lenient().when(environment.getProperty("spring.task.execution.pool.core-size")).thenReturn("2");
        lenient().when(environment.getProperty("spring.task.execution.pool.max-size")).thenReturn("4");
        lenient().when(environment.getProperty("spring.datasource.hikari.minimum-idle")).thenReturn("2");
        lenient().when(environment.getProperty("spring.datasource.hikari.maximum-pool-size")).thenReturn("4");
        lenient().when(environment.getProperty("server.ssl.enabled")).thenReturn("true");
        lenient().when(environment.getProperty("server.ssl.key-store")).thenReturn(null);

        // Execute and Verify
        assertThrows(IllegalStateException.class, () -> validationService.validateConfiguration());
        assertFalse(validationService.isValidationComplete());
    }

    @Test
    void testEnvironmentVariableValidation() {
        // Setup
        lenient().when(environment.getProperty("spring.task.execution.pool.core-size")).thenReturn("2");
        lenient().when(environment.getProperty("spring.task.execution.pool.max-size")).thenReturn("4");
        lenient().when(environment.getProperty("spring.datasource.hikari.minimum-idle")).thenReturn("2");
        lenient().when(environment.getProperty("spring.datasource.hikari.maximum-pool-size")).thenReturn("4");
        lenient().when(environment.getProperty("server.ssl.enabled")).thenReturn("false");

        // Execute
        validationService.validateConfiguration();

        // Verify
        assertTrue(validationService.isValidationComplete());
    }

    @Test
    void testNetworkConnectivityValidation() {
        // Setup
        lenient().when(environment.getProperty("spring.task.execution.pool.core-size")).thenReturn("2");
        lenient().when(environment.getProperty("spring.task.execution.pool.max-size")).thenReturn("4");
        lenient().when(environment.getProperty("spring.datasource.hikari.minimum-idle")).thenReturn("2");
        lenient().when(environment.getProperty("spring.datasource.hikari.maximum-pool-size")).thenReturn("4");
        lenient().when(environment.getProperty("server.ssl.enabled")).thenReturn("false");

        // Execute
        validationService.validateConfiguration();

        // Verify
        assertTrue(validationService.isValidationComplete());
    }

    @Test
    void testResourceAvailabilityValidation() {
        // Setup
        lenient().when(environment.getProperty("spring.task.execution.pool.core-size")).thenReturn("2");
        lenient().when(environment.getProperty("spring.task.execution.pool.max-size")).thenReturn("4");
        lenient().when(environment.getProperty("spring.datasource.hikari.minimum-idle")).thenReturn("2");
        lenient().when(environment.getProperty("spring.datasource.hikari.maximum-pool-size")).thenReturn("4");
        lenient().when(environment.getProperty("server.ssl.enabled")).thenReturn("false");

        // Execute
        validationService.validateConfiguration();

        // Verify
        assertTrue(validationService.isValidationComplete());
    }

    @Test
    void testSolaceConfigurationValidation() {
        // Setup
        lenient().when(environment.getProperty("spring.task.execution.pool.core-size")).thenReturn("2");
        lenient().when(environment.getProperty("spring.task.execution.pool.max-size")).thenReturn("4");
        lenient().when(environment.getProperty("spring.datasource.hikari.minimum-idle")).thenReturn("2");
        lenient().when(environment.getProperty("spring.datasource.hikari.maximum-pool-size")).thenReturn("4");
        lenient().when(environment.getProperty("server.ssl.enabled")).thenReturn("false");

        // Execute
        validationService.validateConfiguration();

        // Verify
        assertTrue(validationService.isValidationComplete());
        verify(solaceProperties).validate();
    }

    @Test
    void testSecurityConfigurationValidation() {
        // Setup
        lenient().when(environment.getProperty("spring.task.execution.pool.core-size")).thenReturn("2");
        lenient().when(environment.getProperty("spring.task.execution.pool.max-size")).thenReturn("4");
        lenient().when(environment.getProperty("spring.datasource.hikari.minimum-idle")).thenReturn("2");
        lenient().when(environment.getProperty("spring.datasource.hikari.maximum-pool-size")).thenReturn("4");
        lenient().when(environment.getProperty("server.ssl.enabled")).thenReturn("false");

        // Execute
        validationService.validateConfiguration();

        // Verify
        assertTrue(validationService.isValidationComplete());
    }

    @Test
    void testPerformanceConfigurationValidation() {
        // Setup
        lenient().when(environment.getProperty("spring.task.execution.pool.core-size")).thenReturn("2");
        lenient().when(environment.getProperty("spring.task.execution.pool.max-size")).thenReturn("4");
        lenient().when(environment.getProperty("spring.datasource.hikari.minimum-idle")).thenReturn("2");
        lenient().when(environment.getProperty("spring.datasource.hikari.maximum-pool-size")).thenReturn("4");
        lenient().when(environment.getProperty("server.ssl.enabled")).thenReturn("false");

        // Execute
        validationService.validateConfiguration();

        // Verify
        assertTrue(validationService.isValidationComplete());
    }

    @Test
    void testValidationFailure() {
        // Setup
        lenient().when(environment.getProperty("spring.task.execution.pool.core-size")).thenReturn("2");
        lenient().when(environment.getProperty("spring.task.execution.pool.max-size")).thenReturn("4");
        lenient().when(environment.getProperty("spring.datasource.hikari.minimum-idle")).thenReturn("2");
        lenient().when(environment.getProperty("spring.datasource.hikari.maximum-pool-size")).thenReturn("4");
        lenient().when(environment.getProperty("server.ssl.enabled")).thenReturn("false");
        
        // Mock Solace configuration to throw an exception
        doThrow(new IllegalStateException("Solace validation failed")).when(solaceProperties).validate();

        // Execute and Verify
        assertThrows(IllegalStateException.class, () -> validationService.validateConfiguration());
        assertFalse(validationService.isValidationComplete());
    }

    @Test
    void testDuplicateValidation() {
        // Setup
        lenient().when(environment.getProperty("spring.task.execution.pool.core-size")).thenReturn("2");
        lenient().when(environment.getProperty("spring.task.execution.pool.max-size")).thenReturn("4");
        lenient().when(environment.getProperty("spring.datasource.hikari.minimum-idle")).thenReturn("2");
        lenient().when(environment.getProperty("spring.datasource.hikari.maximum-pool-size")).thenReturn("4");
        lenient().when(environment.getProperty("server.ssl.enabled")).thenReturn("false");

        // First validation
        validationService.validateConfiguration();
        assertTrue(validationService.isValidationComplete());

        // Second validation should be skipped
        validationService.validateConfiguration();
        assertTrue(validationService.isValidationComplete());
    }
}