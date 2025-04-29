package com.magiccode.tradeingestion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "solace")
public class SolaceProperties {
    private String host;
    private String username;
    private String password;
    private String vpnName;
    private List<Integer> requiredPorts;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVpnName() {
        return vpnName;
    }

    public void setVpnName(String vpnName) {
        this.vpnName = vpnName;
    }

    public List<Integer> getRequiredPorts() {
        return requiredPorts;
    }

    public void setRequiredPorts(List<Integer> requiredPorts) {
        this.requiredPorts = requiredPorts;
    }

    public void validate() {
        if (host == null || host.isEmpty()) {
            throw new IllegalStateException("Solace host is required");
        }
        if (username == null || username.isEmpty()) {
            throw new IllegalStateException("Solace username is required");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalStateException("Solace password is required");
        }
        if (vpnName == null || vpnName.isEmpty()) {
            throw new IllegalStateException("Solace VPN name is required");
        }
        if (requiredPorts == null || requiredPorts.isEmpty()) {
            throw new IllegalStateException("Solace required ports are required");
        }
    }
} 