package com.magiccode.tradeingestion.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SolacePropertiesTest {
    private SolaceProperties properties;
    private Validator validator;

    @BeforeEach
    void setUp() {
        properties = new SolaceProperties();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testDefaultValues() {
        assertEquals("solace/solace-pubsub-standard:latest", properties.getImage());
        assertEquals(List.of(8080, 55556, 8008, 5672, 1883, 9000), properties.getRequiredPorts());
        assertEquals("solace-test", properties.getContainerName());
        assertEquals("default", properties.getVpnName());
        assertEquals(List.of("DEAL.DLQ", "DEAL.IN", "DEAL.OUT"), properties.getRequiredQueues());
        assertEquals("admin", properties.getAdminUsername());
        assertEquals("admin", properties.getAdminPassword());
        assertEquals(Duration.ofMinutes(5), properties.getStartupTimeout());
        assertEquals(4L * 1024L * 1024L * 1024L, properties.getMemoryLimit());
        assertEquals(2L, properties.getCpuCount());
        assertEquals(1024L * 1024L * 1024L, properties.getShmSize());
        assertEquals(60, properties.getMaxWaitAttempts());
        assertEquals(10, properties.getWaitIntervalSeconds());
        assertEquals(3, properties.getMaxRetries());
        assertEquals(Duration.ofSeconds(10), properties.getCheckInterval());
    }

    @Test
    void testValidationSuccess() {
        Set<ConstraintViolation<SolaceProperties>> violations = validator.validate(properties);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", "\n"})
    void testBlankImageValidation(String blankValue) {
        properties.setImage(blankValue);
        Set<ConstraintViolation<SolaceProperties>> violations = validator.validate(properties);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Solace image name must not be blank", violations.iterator().next().getMessage());
    }

    @Test
    void testEmptyRequiredPortsValidation() {
        properties.setRequiredPorts(List.of());
        Set<ConstraintViolation<SolaceProperties>> violations = validator.validate(properties);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Required ports list must not be empty", violations.iterator().next().getMessage());
    }

    @Test
    void testInvalidPortValidation() {
        properties.setRequiredPorts(List.of(0, 70000));
        Set<ConstraintViolation<SolaceProperties>> violations = validator.validate(properties);
        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());
    }

    @Test
    void testMemoryLimitValidation() {
        properties.setMemoryLimit(512L * 1024L * 1024L); // Less than 1GB
        Set<ConstraintViolation<SolaceProperties>> violations = validator.validate(properties);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Memory limit must be at least 1GB", violations.iterator().next().getMessage());
    }

    @Test
    void testCpuCountValidation() {
        properties.setCpuCount(0);
        Set<ConstraintViolation<SolaceProperties>> violations = validator.validate(properties);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("CPU count must be at least 1", violations.iterator().next().getMessage());
    }

    @Test
    void testShmSizeValidation() {
        properties.setShmSize(128L * 1024L * 1024L); // Less than 256MB
        Set<ConstraintViolation<SolaceProperties>> violations = validator.validate(properties);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Shared memory size must be at least 256MB", violations.iterator().next().getMessage());
    }

    @Test
    void testCustomValidationSuccess() {
        properties.validate(); // Should not throw exception
    }

    @Test
    void testCustomValidationPortRange() {
        properties.setRequiredPorts(List.of(0, 70000));
        assertThrows(IllegalStateException.class, () -> properties.validate());
    }

    @Test
    void testCustomValidationMemoryPerCpu() {
        properties.setMemoryLimit(1024L * 1024L * 1024L); // 1GB
        properties.setCpuCount(2L);
        assertThrows(IllegalStateException.class, () -> properties.validate());
    }

    @Test
    void testCustomValidationShmSize() {
        properties.setMemoryLimit(4L * 1024L * 1024L * 1024L); // 4GB
        properties.setShmSize(3L * 1024L * 1024L * 1024L); // 3GB
        assertThrows(IllegalStateException.class, () -> properties.validate());
    }
} 