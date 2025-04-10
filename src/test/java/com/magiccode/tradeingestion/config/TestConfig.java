package com.magiccode.tradeingestion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import jakarta.jms.ConnectionFactory;

@Configuration
public class TestConfig {

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setReconnectOnException(true);
        return connectionFactory;
    }

    @Bean
    public DynamicDestinationResolver destinationResolver() {
        return new DynamicDestinationResolver();
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setPubSubDomain(true);
        return jmsTemplate;
    }
} 