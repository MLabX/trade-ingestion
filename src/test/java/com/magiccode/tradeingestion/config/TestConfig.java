package com.magiccode.tradeingestion.config;

import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;
import jakarta.jms.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TestConfiguration
public class TestConfig {
    private static final Logger logger = LoggerFactory.getLogger(TestConfig.class);

    @Value("${solace.host:tcp://localhost:55555}")
    private String solaceHost;

    @Value("${solace.username:default}")
    private String solaceUsername;

    @Value("${solace.password:default}")
    private String solacePassword;

    @Value("${solace.vpn:default}")
    private String solaceVpn;

    @Bean
    @Primary
    public ConnectionFactory connectionFactory() {
        try {
            logger.info("Creating test Solace connection factory with host: {}", solaceHost);
            SolConnectionFactory connectionFactory = SolJmsUtility.createConnectionFactory();
            connectionFactory.setHost(solaceHost);
            connectionFactory.setUsername(solaceUsername);
            connectionFactory.setPassword(solacePassword);
            connectionFactory.setVPN(solaceVpn);
            return connectionFactory;
        } catch (Exception e) {
            logger.error("Error creating test Solace connection factory: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create test Solace connection factory", e);
        }
    }

    @Bean
    public DestinationResolver destinationResolver() {
        return new DynamicDestinationResolver();
    }

    @Bean
    @Primary
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory, DestinationResolver destinationResolver) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setDestinationResolver(destinationResolver);
        jmsTemplate.setPubSubDomain(true);
        return jmsTemplate;
    }
} 