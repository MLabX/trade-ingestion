package com.magiccode.tradeingestion.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

/**
 * Configuration properties for Solace container management.
 * These properties can be overridden in application.yml or application.properties.
 */
@Configuration
@ConfigurationProperties(prefix = "solace")
@Validated
public class SolaceProperties {
    /**
     * Docker image name for Solace container
     */
    @NotBlank(message = "Solace image name must not be blank")
    private String image = "solace/solace-pubsub-standard:latest";

    /**
     * List of required ports for Solace container
     */
    @NotEmpty(message = "Required ports list must not be empty")
    private List<@Min(value = 1, message = "Port number must be at least 1") 
                @Max(value = 65535, message = "Port number must not exceed 65535") Integer> requiredPorts = List.of(8080, 55556, 8008, 5672, 1883, 9000);

    /**
     * Name of the Solace container
     */
    @NotBlank(message = "Container name must not be blank")
    private String containerName = "solace-test";

    /**
     * VPN name for Solace configuration
     */
    @NotBlank(message = "VPN name must not be blank")
    private String vpnName = "default";

    /**
     * List of required queues for Solace configuration
     */
    @NotEmpty(message = "Required queues list must not be empty")
    private List<@NotBlank String> requiredQueues = List.of("DEAL.DLQ", "DEAL.IN", "DEAL.OUT");

    /**
     * Admin username for Solace
     */
    @NotBlank(message = "Admin username must not be blank")
    private String adminUsername = "admin";

    /**
     * Admin password for Solace
     */
    @NotBlank(message = "Admin password must not be blank")
    private String adminPassword = "admin";

    /**
     * Timeout for container startup
     */
    @NotNull(message = "Startup timeout must not be null")
    private Duration startupTimeout = Duration.ofMinutes(5);

    /**
     * Memory limit for container in bytes
     */
    @Min(value = 1024L * 1024L * 1024L, message = "Memory limit must be at least 1GB")
    private long memoryLimit = 4L * 1024L * 1024L * 1024L; // 4GB

    /**
     * CPU count for container
     */
    @Min(value = 1, message = "CPU count must be at least 1")
    private long cpuCount = 2L;

    /**
     * Shared memory size for container in bytes
     */
    @Min(value = 256L * 1024L * 1024L, message = "Shared memory size must be at least 256MB")
    private long shmSize = 1024L * 1024L * 1024L; // 1GB

    /**
     * Maximum number of wait attempts for container readiness
     */
    @Min(value = 1, message = "Max wait attempts must be at least 1")
    private int maxWaitAttempts = 60;

    /**
     * Interval between wait attempts in seconds
     */
    @Min(value = 1, message = "Wait interval must be at least 1 second")
    private int waitIntervalSeconds = 10;

    /**
     * Maximum number of retries for operations
     */
    @Min(value = 1, message = "Max retries must be at least 1")
    private int maxRetries = 3;

    /**
     * Interval between retries
     */
    @NotNull(message = "Check interval must not be null")
    private Duration checkInterval = Duration.ofSeconds(10);

    // Getters and setters with validation
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    
    public List<Integer> getRequiredPorts() { return requiredPorts; }
    public void setRequiredPorts(List<Integer> requiredPorts) { this.requiredPorts = requiredPorts; }
    
    public String getContainerName() { return containerName; }
    public void setContainerName(String containerName) { this.containerName = containerName; }
    
    public String getVpnName() { return vpnName; }
    public void setVpnName(String vpnName) { this.vpnName = vpnName; }
    
    public List<String> getRequiredQueues() { return requiredQueues; }
    public void setRequiredQueues(List<String> requiredQueues) { this.requiredQueues = requiredQueues; }
    
    public String getAdminUsername() { return adminUsername; }
    public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }
    
    public String getAdminPassword() { return adminPassword; }
    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }
    
    public Duration getStartupTimeout() { return startupTimeout; }
    public void setStartupTimeout(Duration startupTimeout) { this.startupTimeout = startupTimeout; }
    
    public long getMemoryLimit() { return memoryLimit; }
    public void setMemoryLimit(long memoryLimit) { this.memoryLimit = memoryLimit; }
    
    public long getCpuCount() { return cpuCount; }
    public void setCpuCount(long cpuCount) { this.cpuCount = cpuCount; }
    
    public long getShmSize() { return shmSize; }
    public void setShmSize(long shmSize) { this.shmSize = shmSize; }
    
    public int getMaxWaitAttempts() { return maxWaitAttempts; }
    public void setMaxWaitAttempts(int maxWaitAttempts) { this.maxWaitAttempts = maxWaitAttempts; }
    
    public int getWaitIntervalSeconds() { return waitIntervalSeconds; }
    public void setWaitIntervalSeconds(int waitIntervalSeconds) { this.waitIntervalSeconds = waitIntervalSeconds; }
    
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    
    public Duration getCheckInterval() { return checkInterval; }
    public void setCheckInterval(Duration checkInterval) { this.checkInterval = checkInterval; }

    /**
     * Validates the configuration properties
     * @throws IllegalStateException if validation fails
     */
    public void validate() {
        if (requiredPorts.stream().anyMatch(port -> port < 1 || port > 65535)) {
            throw new IllegalStateException("Port numbers must be between 1 and 65535");
        }
        if (memoryLimit < cpuCount * 1024L * 1024L * 1024L) {
            throw new IllegalStateException("Memory limit must be at least 1GB per CPU");
        }
        if (shmSize > memoryLimit / 2) {
            throw new IllegalStateException("Shared memory size must not exceed half of memory limit");
        }
    }
} 