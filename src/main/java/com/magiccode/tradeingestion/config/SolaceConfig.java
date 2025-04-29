package com.magiccode.tradeingestion.config;

import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;
import jakarta.jms.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@EnableJms
@org.springframework.context.annotation.Profile("!test")
public class SolaceConfig {

    @Value("${solace.host}")
    private String host;

    @Value("${solace.port}")
    private int port;

    @Value("${solace.vpn}")
    private String vpn;

    @Value("${solace.username}")
    private String username;

    @Value("${solace.password}")
    private String password;

    @Bean
    public ConnectionFactory connectionFactory() throws Exception {
        SolConnectionFactory solConnectionFactory = SolJmsUtility.createConnectionFactory();
        solConnectionFactory.setHost(host);
        solConnectionFactory.setVPN(vpn);
        solConnectionFactory.setUsername(username);
        solConnectionFactory.setPassword(password);
        solConnectionFactory.setPort(port);

        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setTargetConnectionFactory((ConnectionFactory) solConnectionFactory);
        cachingConnectionFactory.setSessionCacheSize(10);
        return cachingConnectionFactory;
    }

    @Bean
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrency("3-10");
        return factory;
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setPubSubDomain(true);
        return template;
    }
} 
