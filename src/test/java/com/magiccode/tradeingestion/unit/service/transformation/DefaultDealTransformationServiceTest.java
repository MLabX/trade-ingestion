package com.magiccode.tradeingestion.unit.service.transformation;

import com.magiccode.tradeingestion.model.TestDeal;
import com.magiccode.tradeingestion.service.transformation.DefaultDealTransformationService;
import com.magiccode.tradeingestion.unit.LightweightUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DefaultDealTransformationServiceTest extends LightweightUnitTest {

    private DefaultDealTransformationService<TestDeal> transformationService;

    private TestDeal testDeal;

    @BeforeEach
    void setUp() {
        transformationService = new DefaultDealTransformationService<>();
        testDeal = createTestDeal();
    }

    @Test
    void transform_ReturnsSameDealInstance() {
        // Act
        TestDeal result = transformationService.transform(testDeal);

        // Assert
        assertNotNull(result);
        assertSame(testDeal, result, "The transform method should return the same deal instance");
    }

    @Test
    void transform_PreservesAllDealProperties() {
        // Act
        TestDeal result = transformationService.transform(testDeal);

        // Assert
        assertEquals(testDeal.getDealId(), result.getDealId());
        assertEquals(testDeal.getClientId(), result.getClientId());
        assertEquals(testDeal.getInstrumentId(), result.getInstrumentId());
        assertEquals(testDeal.getQuantity(), result.getQuantity());
        assertEquals(testDeal.getPrice(), result.getPrice());
        assertEquals(testDeal.getCurrency(), result.getCurrency());
        assertEquals(testDeal.getStatus(), result.getStatus());
        assertEquals(testDeal.getDealDate(), result.getDealDate());
    }

    private TestDeal createTestDeal() {
        TestDeal deal = new TestDeal();
        deal.setDealId("TEST-DEAL-001");
        deal.setClientId("CLIENT001");
        deal.setInstrumentId("INST001");
        deal.setQuantity(new BigDecimal("100"));
        deal.setPrice(new BigDecimal("10.5"));
        deal.setCurrency("USD");
        deal.setStatus("NEW");
        deal.setDealDate(LocalDateTime.now());
        return deal;
    }
}
