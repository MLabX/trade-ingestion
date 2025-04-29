package com.magiccode.tradeingestion.config;

import org.testcontainers.utility.DockerImageName;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PreDestroy;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Configuration class for Solace container using Testcontainers.
 * Sets up a Solace container with appropriate environment variables and wait strategy.
 */
@Configuration
public class SolaceContainerConfig {
    private static final Logger logger = LoggerFactory.getLogger(SolaceContainerConfig.class);
    
    private final SolaceProperties properties;
    private static final AtomicReference<GenericContainer<?>> containerRef = new AtomicReference<>();

    @Autowired
    public SolaceContainerConfig(SolaceProperties properties) {
        this.properties = properties;
    }

    @Bean(destroyMethod = "stop")
    public GenericContainer<?> solaceContainer() {
        return containerRef.updateAndGet(existing -> {
            if (existing != null && existing.isRunning()) {
                return existing;
            }

            try {
                // Use built-in wait strategies
                WaitAllStrategy waitStrategy = new WaitAllStrategy()
                    .withStartupTimeout(properties.getStartupTimeout())
                    .withStrategy(Wait.forLogMessage(".*Solace PubSub\\+ Standard Edition.*", 1))
                    .withStrategy(Wait.forLogMessage(".*Solace Message Router is ready for client connections.*", 1))
                    .withStrategy(Wait.forHttp("/SEMP/v2/config/about/api")
                        .withBasicCredentials(properties.getAdminUsername(), properties.getAdminPassword())
                        .forStatusCode(200));

                GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(properties.getImage()))
                    .withExposedPorts(properties.getRequiredPorts().toArray(new Integer[0]))
                    .withEnv("username_admin_globalaccesslevel", "admin")
                    .withEnv("username_admin_password", properties.getAdminPassword())
                    .withEnv("system_scaling_maxconnectioncount", "100")
                    .withEnv("system_scaling_maxqueuesize", "500")
                    .withEnv("service_semp_plaintext_port", "8080")
                    .withEnv("service_smf_port", "55555")
                    .withEnv("service_smf_compressed_port", "55003")
                    .withEnv("service_web_transport_port", "8008")
                    .withEnv("service_mqtt_tcp_port", "1883")
                    .withEnv("service_amqp_port", "5672")
                    .withStartupTimeout(properties.getStartupTimeout())
                    .withCreateContainerCmdModifier(cmd -> {
                        cmd.getHostConfig()
                            .withMemory(properties.getMemoryLimit())
                            .withCpuCount(properties.getCpuCount())
                            .withShmSize(properties.getShmSize());
                    })
                    .waitingFor(waitStrategy);

                container.start();
                logger.info("Solace container started successfully. Mapped ports:");
                properties.getRequiredPorts().forEach(port -> 
                    logger.info("  {} -> {}", port, container.getMappedPort(port))
                );
                
                return container;
            } catch (Exception e) {
                logger.error("Failed to create Solace container", e);
                throw new RuntimeException("Failed to create Solace container", e);
            }
        });
    }

    @PreDestroy
    public void cleanup() {
        GenericContainer<?> container = containerRef.get();
        if (container != null && container.isRunning()) {
            try {
                container.stop();
                logger.info("Solace container stopped successfully");
            } catch (Exception e) {
                logger.error("Error stopping Solace container", e);
            }
        }
    }
} 