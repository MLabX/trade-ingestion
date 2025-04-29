package com.magiccode.tradeingestion;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import com.magiccode.tradeingestion.config.BaseIntegrationTestConfig;

@SpringBootTest
@ActiveProfiles("integration")
@Import(BaseIntegrationTestConfig.class)
public abstract class BaseIntegrationTest {
    // Base class for all integration tests
    // Container management is now handled by BaseIntegrationTestConfig
} 