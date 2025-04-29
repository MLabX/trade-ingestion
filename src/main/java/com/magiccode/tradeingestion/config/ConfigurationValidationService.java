package com.magiccode.tradeingestion.config;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class ConfigurationValidationService {

    private final Environment environment;
    private final SolaceProperties solaceProperties;
    private boolean validationComplete = false;

    public ConfigurationValidationService(Environment environment, SolaceProperties solaceProperties) {
        this.environment = environment;
        this.solaceProperties = solaceProperties;
    }

    public void validateConfiguration() {
        if (validationComplete) {
            return;
        }

        validateEnvironmentVariables();
        validateNetworkConnectivity();
        validateResourceAvailability();
        validateSolaceConfiguration();
        validateSecurityConfiguration();
        validatePerformanceConfiguration();

        validationComplete = true;
    }

    private void validateEnvironmentVariables() {
        String activeProfile = environment.getProperty("spring.profiles.active");
        if (activeProfile == null || activeProfile.trim().isEmpty()) {
            throw new IllegalStateException("spring.profiles.active property is required");
        }
    }

    private void validateNetworkConnectivity() {
        if (solaceProperties.getRequiredPorts() == null || solaceProperties.getRequiredPorts().isEmpty()) {
            throw new IllegalStateException("Solace required ports configuration is missing");
        }
    }

    private void validateResourceAvailability() {
        String coreSize = environment.getProperty("spring.task.execution.pool.core-size");
        String maxSize = environment.getProperty("spring.task.execution.pool.max-size");
        
        if (coreSize != null && maxSize != null) {
            int core = Integer.parseInt(coreSize);
            int max = Integer.parseInt(maxSize);
            if (core > max) {
                throw new IllegalStateException("Thread pool core size cannot be greater than max size");
            }
        }
    }

    private void validateSolaceConfiguration() {
        solaceProperties.validate();
    }

    private void validateSecurityConfiguration() {
        String sslEnabled = environment.getProperty("server.ssl.enabled");
        if ("true".equals(sslEnabled)) {
            String keyStore = environment.getProperty("server.ssl.key-store");
            String keyStorePassword = environment.getProperty("server.ssl.key-store-password");
            String keyStoreType = environment.getProperty("server.ssl.key-store-type");
            
            if (keyStore == null || keyStorePassword == null || keyStoreType == null) {
                throw new IllegalStateException("SSL configuration is incomplete");
            }
        }
    }

    private void validatePerformanceConfiguration() {
        String minIdle = environment.getProperty("spring.datasource.hikari.minimum-idle");
        String maxPoolSize = environment.getProperty("spring.datasource.hikari.maximum-pool-size");
        
        if (minIdle != null && maxPoolSize != null) {
            int min = Integer.parseInt(minIdle);
            int max = Integer.parseInt(maxPoolSize);
            if (min > max) {
                throw new IllegalStateException("Connection pool minimum idle cannot be greater than maximum pool size");
            }
        }
    }

    boolean isValidationComplete() {
        return validationComplete;
    }
} 