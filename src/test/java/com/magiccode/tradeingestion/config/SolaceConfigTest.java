package com.magiccode.tradeingestion.config;

import jakarta.jms.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestPropertySource(properties = {
    "solace.jms.enabled=true",
    "solace.host=localhost",
    "solace.username=test",
    "solace.password=test",
    "solace.vpn=default"
})
public class SolaceConfigTest {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Test
    public void testConnectionFactoryBean() {
        assertNotNull(connectionFactory, "ConnectionFactory should not be null");
    }

    @Test
    public void testJmsTemplateBean() {
        assertNotNull(jmsTemplate, "JmsTemplate should not be null");
    }
} 