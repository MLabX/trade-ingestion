package com.magiccode.tradeingestion.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategyTarget;
import org.testcontainers.containers.wait.strategy.Wait;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class SolaceContainerReadyStrategy extends AbstractWaitStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(SolaceContainerReadyStrategy.class);
    
    private final SolaceProperties properties;

    @Autowired
    public SolaceContainerReadyStrategy(SolaceProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void waitUntilReady() {
        if (!(waitStrategyTarget instanceof GenericContainer)) {
            throw new IllegalStateException("Target must be a GenericContainer");
        }

        GenericContainer<?> container = (GenericContainer<?>) waitStrategyTarget;
        String host = container.getHost();
        int port = container.getMappedPort(8080);

        LOGGER.info("Waiting for Solace container to be ready on {}:{}", host, port);

        // First wait for the container to be running and logs to show startup
        Wait.forLogMessage(".*Solace PubSub\\+ Standard Edition.*", 1)
            .withStartupTimeout(properties.getStartupTimeout())
            .waitUntilReady(container);

        // Then check SEMP API and queues
        for (int retry = 0; retry < properties.getMaxRetries(); retry++) {
            try {
                if (isSempApiReady(host, port) && areQueuesReady(host, port)) {
                    LOGGER.info("Solace container is ready");
                    return;
                }
            } catch (Exception e) {
                LOGGER.warn("Attempt {} failed: {}", retry + 1, e.getMessage());
                if (retry == properties.getMaxRetries() - 1) {
                    throw new RuntimeException("Failed to verify Solace container readiness after " + 
                        properties.getMaxRetries() + " attempts", e);
                }
            }
            try {
                TimeUnit.SECONDS.sleep(properties.getCheckInterval().getSeconds());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for Solace container", e);
            }
        }
    }

    private boolean isSempApiReady(String host, int port) {
        String url = String.format("http://%s:%d/SEMP/v2/config/about/api", host, port);
        String command = String.format("curl -s -o /dev/null -w \"%%{http_code}\" -u %s:%s %s", 
            properties.getAdminUsername(), properties.getAdminPassword(), url);
        
        try {
            String result = executeCommand(command);
            int statusCode = Integer.parseInt(result);
            return statusCode == 200;
        } catch (Exception e) {
            LOGGER.warn("Failed to check SEMP API: {}", e.getMessage());
            return false;
        }
    }

    private boolean areQueuesReady(String host, int port) {
        for (String queue : properties.getRequiredQueues()) {
            String url = String.format("http://%s:%d/SEMP/v2/config/msgVpns/%s/queues/%s", 
                host, port, properties.getVpnName(), queue);
            String command = String.format("curl -s -o /dev/null -w \"%%{http_code}\" -u %s:%s %s", 
                properties.getAdminUsername(), properties.getAdminPassword(), url);
            
            try {
                String result = executeCommand(command);
                int statusCode = Integer.parseInt(result);
                if (statusCode != 200) {
                    LOGGER.warn("Queue {} not ready (status: {})", queue, statusCode);
                    return false;
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to check queue {}: {}", queue, e.getMessage());
                return false;
            }
        }
        return true;
    }

    private String executeCommand(String command) throws Exception {
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor(10, TimeUnit.SECONDS);
        if (process.exitValue() != 0) {
            throw new RuntimeException("Command failed with exit code: " + process.exitValue());
        }
        return new String(process.getInputStream().readAllBytes()).trim();
    }
} 