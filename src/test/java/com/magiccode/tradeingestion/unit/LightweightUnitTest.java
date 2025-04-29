package com.magiccode.tradeingestion.unit;

import com.magiccode.tradeingestion.testdata.TestDataFactory;
import com.magiccode.tradeingestion.testdata.TestDataConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

/**
 * Base class for lightweight unit tests that don't require a full Spring context.
 * This class provides:
 * 1. TestDataFactory injection
 * 2. Mockito support
 * 3. Basic Spring test support without full context loading
 */
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {TestDataFactory.class, TestDataConfig.class, ObjectMapper.class})
public abstract class LightweightUnitTest {
    @MockBean
    protected TestDataFactory testDataFactory;
} 