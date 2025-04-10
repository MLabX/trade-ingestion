package com.magiccode.tradeingestion.integration;

import com.magiccode.tradeingestion.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestConfig.class)
public class TradeIntegrationTest {

    @Test
    void contextLoads() {
    }
}
