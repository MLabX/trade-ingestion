package com.magiccode.tradeingestion.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Manages the lifecycle and configuration of a Solace container for testing purposes.
 * 
 * This class handles:
 * 1. Container startup and shutdown
 * 2. Port availability checking
 * 3. Container status verification
 * 4. Solace configuration (VPN, queues, subscriptions)
 * 5. Resource cleanup
 * 
 * The manager ensures that the Solace container is properly configured
 * and ready for use in test environments.
 */
@Component
@Import(SolaceContainerConfig.class)
public class SolaceContainerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SolaceContainerManager.class);

    private final GenericContainer<?> testContainer;
    private final SolaceProperties properties;
    private String existingContainerId;
    private int solacePort;

    /**
     * Constructs a new SolaceContainerManager with the specified configuration.
     *
     * @param solaceContainerConfig The container configuration
     * @param properties The Solace properties
     */
    @Autowired
    public SolaceContainerManager(
            final SolaceContainerConfig solaceContainerConfig, 
            final SolaceProperties properties) {
        this.testContainer = solaceContainerConfig.solaceContainer()
            .withLogConsumer(outputFrame -> {
                String log = outputFrame.getUtf8String();
                if (log.contains("ERROR") || log.contains("error")) {
                    LOGGER.error("Container log: {}", log);
                } else if (log.contains("WARN") || log.contains("warn")) {
                    LOGGER.warn("Container log: {}", log);
                } else {
                    LOGGER.info("Container log: {}", log);
                }
            });
        this.properties = properties;
    }

    /**
     * Starts the Solace container and configures it for testing.
     * 
     * This method:
     * 1. Checks for existing containers
     * 2. Verifies port availability
     * 3. Starts the container
     * 4. Configures Solace settings
     * 
     * @throws IllegalStateException if ports are in use or container startup fails
     */
    public void start() {
        long startTime = System.currentTimeMillis();
        LOGGER.info("Starting Solace container...");

        try {
            // Check for existing Solace containers
            String existingContainer = findExistingSolaceContainer();
            if (existingContainer != null) {
                LOGGER.info("Found existing Solace container: {}", existingContainer);
                this.existingContainerId = existingContainer;
                return;
            }

            // Check if ports are in use
            if (isPortInUse()) {
                throw new IllegalStateException(
                    "Required ports are already in use. Please stop any running Solace containers.");
            }

            testContainer.start();
            long containerStartTime = System.currentTimeMillis();
            LOGGER.info("Container started in {} ms", containerStartTime - startTime);

            // Get the mapped port for SEMP API
            int sempPort = testContainer.getMappedPort(8080);
            LOGGER.info("Solace container started. SEMP API port mapped to: {}", sempPort);

            // Check container status
            String containerId = testContainer.getContainerId();
            try {
                Process process = Runtime.getRuntime().exec(
                    String.format("docker inspect -f '{{.State.Status}}' %s", containerId));
                process.waitFor(5, TimeUnit.SECONDS);
                String status = new String(process.getInputStream().readAllBytes()).trim();
                LOGGER.info("Container status: {}", status);

                // Get container logs
                process = Runtime.getRuntime().exec(
                    String.format("docker logs %s", containerId));
                process.waitFor(5, TimeUnit.SECONDS);
                String logs = new String(process.getInputStream().readAllBytes());
                LOGGER.info("Container logs:\n{}", logs);
            } catch (Exception e) {
                LOGGER.warn("Failed to get container status: {}", e.getMessage());
            }

            LOGGER.info("Configuring Solace...");
            configureSolace();
            long endTime = System.currentTimeMillis();
            LOGGER.info("Solace container configuration completed in {} ms (total startup time: {} ms)", 
                endTime - containerStartTime, endTime - startTime);
        } catch (Exception e) {
            LOGGER.error("Failed to start Solace container: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to start Solace container", e);
        }
    }

    /**
     * Stops the Solace container if it was started by this manager.
     * Existing containers are not stopped.
     */
    public void stop() {
        if (existingContainerId != null) {
            LOGGER.info("Not stopping existing container: {}", existingContainerId);
            return;
        }
        if (testContainer != null) {
            testContainer.stop();
        }
    }

    /**
     * Configures the Solace container with required settings.
     * 
     * This method:
     * 1. Waits for SEMP API to be ready
     * 2. Configures the VPN
     * 3. Creates required queues
     * 4. Sets up subscriptions
     */
    private void configureSolace() {
        String host = testContainer.getHost();
        int port = testContainer.getMappedPort(8080);

        LOGGER.info("Using Solace SEMP API at {}:{}", host, port);

        // Wait for SEMP API to be ready
        waitForSempApi(host, port);

        // Configure VPN
        configureVpn(host, port);

        // Configure queues
        for (String queue : properties.getRequiredQueues()) {
            configureQueue(host, port, queue);
        }

        // Configure subscriptions
        configureSubscription(host, port, "DEAL.IN", "DEAL.IN");
    }

    /**
     * Waits for the SEMP API to be ready.
     *
     * @param host The container host
     * @param port The SEMP API port
     * @throws IllegalStateException if the API is not ready after maximum attempts
     */
    private void waitForSempApi(final String host, final int port) {
        LOGGER.info("Waiting for SEMP API to be ready on {}:{}...", host, port);
        int attempt = 0;
        Exception lastException = null;

        while (attempt < properties.getMaxWaitAttempts()) {
            try {
                URL url = new URL(String.format("http://%s:%d/SEMP/v2/config/about/api", 
                    host, port));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", 
                    "Basic " + Base64.getEncoder().encodeToString(
                        (properties.getAdminUsername() + ":" + properties.getAdminPassword())
                            .getBytes()));

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    LOGGER.info("SEMP API is ready");
                    return;
                }
            } catch (Exception e) {
                lastException = e;
                LOGGER.warn("Attempt {} failed: {}", attempt + 1, e.getMessage());
            }

            try {
                TimeUnit.SECONDS.sleep(properties.getWaitIntervalSeconds());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for SEMP API", e);
            }
            attempt++;
        }

        throw new IllegalStateException(
            "SEMP API not ready after " + properties.getMaxWaitAttempts() + " attempts", 
            lastException);
    }

    /**
     * Configures the VPN in the Solace container.
     *
     * @param host The container host
     * @param port The SEMP API port
     */
    private void configureVpn(final String host, final int port) {
        LOGGER.info("Configuring VPN: {}", properties.getVpnName());
        String command = String.format(
            "curl -s -X PATCH " +
            "-H \"Content-Type: application/json\" " +
            "-u %s:%s " +
            "http://%s:%d/SEMP/v2/config/msgVpns/%s " +
            "-d '{\"enabled\":true,\"maxConnectionCount\":100,\"maxEgressFlowCount\":100," +
            "\"maxIngressFlowCount\":100,\"maxSubscriptionCount\":100,\"maxTransactionCount\":100," +
            "\"maxTransactedSessionCount\":100}'",
            properties.getAdminUsername(), properties.getAdminPassword(),
            host, port, properties.getVpnName()
        );

        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor(10, TimeUnit.SECONDS);

            if (process.exitValue() != 0) {
                LOGGER.warn("Failed to configure VPN {}, might already be configured", properties.getVpnName());
            } else {
                LOGGER.info("VPN {} configured successfully", properties.getVpnName());
            }
        } catch (Exception e) {
            LOGGER.warn("Error configuring VPN {}: {}", properties.getVpnName(), e.getMessage());
        }
    }

    /**
     * Configures a queue in the Solace container.
     *
     * @param host The container host
     * @param port The SEMP API port
     * @param queueName The name of the queue to configure
     */
    private void configureQueue(final String host, final int port, final String queueName) {
        LOGGER.info("Configuring queue: {}", queueName);
        String command = String.format(
            "curl -s -X POST " +
            "-H \"Content-Type: application/json\" " +
            "-u %s:%s " +
            "http://%s:%d/SEMP/v2/config/msgVpns/%s/queues " +
            "-d '{\"queueName\":\"%s\",\"accessType\":\"exclusive\",\"permission\":\"consume\"," +
            "\"ingressEnabled\":true,\"egressEnabled\":true,\"respectTtlEnabled\":true," +
            "\"maxMsgSize\":10000000,\"maxMsgSpoolUsage\":500}'",
            properties.getAdminUsername(), properties.getAdminPassword(),
            host, port, properties.getVpnName(), queueName
        );

        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor(10, TimeUnit.SECONDS);

            if (process.exitValue() != 0) {
                LOGGER.warn("Failed to configure queue {}, might already exist", queueName);
            } else {
                LOGGER.info("Queue {} configured successfully", queueName);
            }
        } catch (Exception e) {
            LOGGER.warn("Error configuring queue {}: {}", queueName, e.getMessage());
        }
    }

    /**
     * Configures a subscription for a queue in the Solace container.
     *
     * @param host The container host
     * @param port The SEMP API port
     * @param queueName The name of the queue
     * @param topic The topic to subscribe to
     */
    private void configureSubscription(
            final String host, 
            final int port, 
            final String queueName, 
            final String topic) {
        LOGGER.info("Configuring subscription for queue {} to topic {}", queueName, topic);
        String command = String.format(
            "curl -s -X POST " +
            "-H \"Content-Type: application/json\" " +
            "-u %s:%s " +
            "http://%s:%d/SEMP/v2/config/msgVpns/%s/queues/%s/subscriptions " +
            "-d '{\"subscriptionTopic\":\"%s\"}'",
            properties.getAdminUsername(), properties.getAdminPassword(),
            host, port, properties.getVpnName(), queueName, topic
        );

        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor(10, TimeUnit.SECONDS);

            if (process.exitValue() != 0) {
                LOGGER.warn("Failed to configure subscription for queue {}, might already exist", 
                    queueName);
            } else {
                LOGGER.info("Subscription configured successfully for queue {}", queueName);
            }
        } catch (Exception e) {
            LOGGER.warn("Error configuring subscription for queue {}: {}", queueName, e.getMessage());
        }
    }

    /**
     * Finds an existing Solace container.
     *
     * @return The container ID if found, null otherwise
     */
    private String findExistingSolaceContainer() {
        try {
            Process process = Runtime.getRuntime().exec(
                "docker ps -q -f name=" + properties.getContainerName());
            process.waitFor(5, TimeUnit.SECONDS);

            if (process.exitValue() == 0) {
                try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                    return reader.readLine();
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Error checking for existing Solace container: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Checks if any of the required ports are in use.
     *
     * @return true if any port is in use, false otherwise
     */
    private boolean isPortInUse() {
        for (int port : properties.getRequiredPorts()) {
            try {
                Process process = Runtime.getRuntime().exec(
                    String.format("lsof -i :%d", port));
                process.waitFor(1, TimeUnit.SECONDS);
                if (process.exitValue() == 0) {
                    return true;
                }
            } catch (Exception e) {
                LOGGER.warn("Error checking port {}: {}", port, e.getMessage());
            }
        }
        return false;
    }

    /**
     * Gets the container host.
     *
     * @return The container host
     */
    public String getContainerHost() {
        return testContainer.getHost();
    }

    /**
     * Gets the mapped port for a given original port.
     *
     * @param originalPort The original port
     * @return The mapped port
     */
    public int getContainerPort(final int originalPort) {
        return testContainer.getMappedPort(originalPort);
    }

    /**
     * Gets the Solace port.
     *
     * @return The Solace port
     */
    public int getSolacePort() {
        return solacePort;
    }
} 
