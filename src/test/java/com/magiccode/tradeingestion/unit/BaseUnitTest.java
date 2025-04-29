package com.magiccode.tradeingestion.unit;

import com.magiccode.tradeingestion.testdata.TestDataFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SpringBootTest
public abstract class BaseUnitTest {
    @Autowired
    protected TestDataFactory testDataFactory;
} 