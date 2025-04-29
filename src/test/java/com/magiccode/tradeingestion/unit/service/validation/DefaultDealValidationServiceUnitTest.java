package com.magiccode.tradeingestion.unit.service.validation;

import com.magiccode.tradeingestion.model.TestDeal;
import com.magiccode.tradeingestion.service.validation.DefaultDealValidationService;
import com.magiccode.tradeingestion.unit.LightweightUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultDealValidationServiceUnitTest extends LightweightUnitTest {

    private DefaultDealValidationService validationService;
    private TestDeal validDeal;

    @BeforeEach
    void setUp() {
        validationService = new DefaultDealValidationService();
        validDeal = createValidTestDeal();
    }

    @Test
    void validateDeal_ValidDeal_DoesNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> validationService.validateDeal(validDeal));
    }

    @Test
    void validateDeal_NullDeal_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validationService.validateDeal(null));
    }

    @Test
    void validateDeal_NullDealId_ThrowsException() {
        // Arrange
        validDeal.setDealId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validationService.validateDeal(validDeal));
    }

    @Test
    void validateDeal_EmptyDealId_ThrowsException() {
        // Arrange
        validDeal.setDealId("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validationService.validateDeal(validDeal));
    }

    @Test
    void validateDeal_BlankDealId_ThrowsException() {
        // Arrange
        validDeal.setDealId("   ");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validationService.validateDeal(validDeal));
    }

    @Test
    void validateDeal_NullClientId_ThrowsException() {
        // Arrange
        validDeal.setClientId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validationService.validateDeal(validDeal));
    }

    @Test
    void validateDeal_EmptyClientId_ThrowsException() {
        // Arrange
        validDeal.setClientId("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validationService.validateDeal(validDeal));
    }

    @Test
    void validateDeal_BlankClientId_ThrowsException() {
        // Arrange
        validDeal.setClientId("   ");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validationService.validateDeal(validDeal));
    }

    @Test
    void validateDeal_NullInstrumentId_ThrowsException() {
        // Arrange
        validDeal.setInstrumentId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validationService.validateDeal(validDeal));
    }

    @Test
    void validateDeal_EmptyInstrumentId_ThrowsException() {
        // Arrange
        validDeal.setInstrumentId("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validationService.validateDeal(validDeal));
    }

    @Test
    void validateDeal_BlankInstrumentId_ThrowsException() {
        // Arrange
        validDeal.setInstrumentId("   ");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validationService.validateDeal(validDeal));
    }

    @Test
    void validateDeal_NullQuantity_ThrowsException() {
        // Arrange
        validDeal.setQuantity(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validationService.validateDeal(validDeal));
    }

    @Test
    void validateDeal_ZeroQuantity_ThrowsException() {
        // Arrange
        validDeal.setQuantity(BigDecimal.ZERO);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validationService.validateDeal(validDeal));
    }

    @Test
    void validateDeal_NegativeQuantity_ThrowsException() {
        // Arrange
        validDeal.setQuantity(new BigDecimal("-10"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validationService.validateDeal(validDeal));
    }

    @Test
    void validateDeal_NullPrice_ThrowsException() {
        // Arrange
        validDeal.setPrice(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validationService.validateDeal(validDeal));
    }

    @Test
    void validateDeal_ZeroPrice_ThrowsException() {
        // Arrange
        validDeal.setPrice(BigDecimal.ZERO);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validationService.validateDeal(validDeal));
    }

    @Test
    void validateDeal_NegativePrice_ThrowsException() {
        // Arrange
        validDeal.setPrice(new BigDecimal("-10.5"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validationService.validateDeal(validDeal));
    }

    @Test
    void validateDeal_NullCurrency_ThrowsException() {
        // Arrange
        validDeal.setCurrency(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validationService.validateDeal(validDeal));
    }

    @Test
    void validateDeal_InvalidCurrencyFormat_ThrowsException() {
        // Arrange
        validDeal.setCurrency("USDD");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validationService.validateDeal(validDeal));
    }

    @Test
    void validateDeal_LowercaseCurrency_ThrowsException() {
        // Arrange
        validDeal.setCurrency("usd");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validationService.validateDeal(validDeal));
    }

    @Test
    void validateDeal_NullStatus_ThrowsException() {
        // Arrange
        validDeal.setStatus(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validationService.validateDeal(validDeal));
    }

    @Test
    void validateDeal_InvalidStatus_ThrowsException() {
        // Arrange
        validDeal.setStatus("INVALID_STATUS");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validationService.validateDeal(validDeal));
    }

    @Test
    void validateDeal_NullDealDate_ThrowsException() {
        // Arrange
        validDeal.setDealDate(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validationService.validateDeal(validDeal));
    }

    private TestDeal createValidTestDeal() {
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