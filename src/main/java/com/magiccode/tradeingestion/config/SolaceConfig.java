package com.magiccode.tradeingestion.config;

import jakarta.jms.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;

import java.math.BigDecimal;

@Configuration
@ConditionalOnProperty(name = "solace.jms.enabled", havingValue = "true", matchIfMissing = true)
public class SolaceConfig {

    private static final Logger logger = LoggerFactory.getLogger(SolaceConfig.class);

    @Value("${solace.jms.enabled}")
    private boolean enabled;

    @Value("${solace.ssl.enabled}")
    private boolean sslEnabled;

    @Value("${solace.ssl.truststore}")
    private String truststore;

    @Value("${solace.ssl.truststore.password}")
    private String truststorePassword;

    @Value("${solace.host}")
    private String host;

    @Value("${solace.username}")
    private String username;

    @Value("${solace.password}")
    private String password;

    @Value("${solace.vpn}")
    private String vpn;

    // Getter methods for testing
    public String getHost() {
        return host;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getVpn() {
        return vpn;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        try {
            // Create the Solace connection factory
            SolConnectionFactory factory = SolJmsUtility.createConnectionFactory();
            
            // Set connection properties
            factory.setHost(host);
            factory.setUsername(username);
            factory.setPassword(password);
            factory.setVPN(vpn);
            
            // Configure SSL if enabled
            if (sslEnabled) {
                factory.setSSLTrustStore(truststore);
                factory.setSSLTrustStorePassword(truststorePassword);
            }
            
            logger.info("Created Solace connection factory with host: {}", host);
            
            // Wrap in caching connection factory for better performance
            CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory((ConnectionFactory) factory);
            cachingConnectionFactory.setSessionCacheSize(10);
            
            return cachingConnectionFactory;
        } catch (Exception e) {
            logger.error("Error creating Solace connection factory: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating Solace connection factory: " + e.getMessage(), e);
        }
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrency("1-1");
        return factory;
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setPubSubDomain(true);
        return jmsTemplate;
    }
} 
