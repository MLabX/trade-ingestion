package com.magiccode.tradeingestion.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
public class ApplicationPropertiesTest {

    @Autowired
    private SolaceConfig solaceConfig;

    @Test
    public void testSolaceProperties() {
        // These values should match what's in application-test.yml
        assertEquals("localhost", solaceConfig.getHost());
        assertEquals("test", solaceConfig.getUsername());
        assertEquals("test", solaceConfig.getPassword());
        assertEquals("default", solaceConfig.getVpn());
    }
} 