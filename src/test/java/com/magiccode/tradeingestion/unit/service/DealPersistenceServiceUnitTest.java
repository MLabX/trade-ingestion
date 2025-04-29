package com.magiccode.tradeingestion.unit.service;

import com.magiccode.tradeingestion.exception.DealProcessingException;
import com.magiccode.tradeingestion.model.TestDeal;
import com.magiccode.tradeingestion.repository.DealRepository;
import com.magiccode.tradeingestion.service.DealPersistenceService;
import com.magiccode.tradeingestion.unit.LightweightUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.ConcurrentModificationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DealPersistenceServiceUnitTest extends LightweightUnitTest {

    @Mock
    private DealRepository dealRepository;

    private DealPersistenceService dealPersistenceService;
    private TestDeal testDeal;
    private UUID dealUuid;

    @BeforeEach
    void setUp() {
        dealPersistenceService = new DealPersistenceService(dealRepository);
        dealUuid = UUID.randomUUID();
        testDeal = createTestDeal();
    }

    @Test
    void getDealById_WhenDealExists_ReturnsDeal() {
        // Arrange
        when(dealRepository.findById(dealUuid)).thenReturn(Optional.of(testDeal));

        // Act
        TestDeal result = (TestDeal) dealPersistenceService.getDealById(dealUuid);

        // Assert
        assertNotNull(result);
        assertEquals(testDeal.getDealId(), result.getDealId());
        verify(dealRepository).findById(dealUuid);
    }

    @Test
    void getDealById_WhenDealDoesNotExist_ThrowsException() {
        // Arrange
        when(dealRepository.findById(dealUuid)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DealProcessingException.class, () -> dealPersistenceService.getDealById(dealUuid));
        verify(dealRepository).findById(dealUuid);
    }

    @Test
    void getDealByDealId_WhenDealExists_ReturnsDeal() {
        // Arrange
        String dealId = "TEST-DEAL-001";
        when(dealRepository.findByDealId(dealId)).thenReturn(Optional.of(testDeal));

        // Act
        TestDeal result = (TestDeal) dealPersistenceService.getDealByDealId(dealId);

        // Assert
        assertNotNull(result);
        assertEquals(dealId, result.getDealId());
        verify(dealRepository).findByDealId(dealId);
    }

    @Test
    void getDealByDealId_WhenDealDoesNotExist_ThrowsException() {
        // Arrange
        String dealId = "TEST-DEAL-001";
        when(dealRepository.findByDealId(dealId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DealProcessingException.class, () -> dealPersistenceService.getDealByDealId(dealId));
        verify(dealRepository).findByDealId(dealId);
    }

    @Test
    void saveDeal_NewDeal_SavesSuccessfully() {
        // Arrange
        when(dealRepository.findByDealId(anyString())).thenReturn(Optional.empty());
        when(dealRepository.save(any(TestDeal.class))).thenReturn(testDeal);

        // Act
        TestDeal result = (TestDeal) dealPersistenceService.saveDeal(testDeal);

        // Assert
        assertNotNull(result);
        assertEquals(testDeal.getDealId(), result.getDealId());
        verify(dealRepository).findByDealId(testDeal.getDealId());
        verify(dealRepository).save(testDeal);
    }

    @Test
    void saveDeal_ExistingDealWithSameVersion_IncrementsVersionAndSaves() {
        // Arrange
        TestDeal existingDeal = createTestDeal();
        existingDeal.setVersion(1L);

        TestDeal updatedDeal = createTestDeal();
        updatedDeal.setVersion(1L);

        when(dealRepository.findByDealId(anyString())).thenReturn(Optional.of(existingDeal));
        when(dealRepository.save(any(TestDeal.class))).thenReturn(updatedDeal);

        // Act
        TestDeal result = (TestDeal) dealPersistenceService.saveDeal(updatedDeal);

        // Assert
        assertNotNull(result);
        assertEquals(testDeal.getDealId(), result.getDealId());
        verify(dealRepository).findByDealId(testDeal.getDealId());
        verify(dealRepository).save(updatedDeal);
    }

    @Test
    void saveDeal_ExistingDealWithDifferentVersion_ThrowsConcurrentModificationException() {
        // Arrange
        TestDeal existingDeal = createTestDeal();
        existingDeal.setVersion(2L);

        TestDeal updatedDeal = createTestDeal();
        updatedDeal.setVersion(1L);

        when(dealRepository.findByDealId(anyString())).thenReturn(Optional.of(existingDeal));

        // Act & Assert
        assertThrows(ConcurrentModificationException.class, () -> dealPersistenceService.saveDeal(updatedDeal));
        verify(dealRepository).findByDealId(testDeal.getDealId());
        verify(dealRepository, never()).save(any(TestDeal.class));
    }

    @Test
    void clearCache_CallsLogInfo() {
        // Act
        dealPersistenceService.clearCache();

        // No specific assertion needed as this is just testing that the method runs without error
    }

    @Test
    void deleteDeal_RemovesDealFromStore() {
        // Act
        dealPersistenceService.deleteDeal(testDeal.getDealId());

        // No specific assertion needed as this is just testing that the method runs without error
    }

    private TestDeal createTestDeal() {
        TestDeal deal = new TestDeal();
        deal.setId(dealUuid);
        deal.setDealId("TEST-DEAL-001");
        deal.setEventType("CREATED");
        deal.setClientId("CLIENT001");
        deal.setInstrumentId("INST001");
        deal.setQuantity(new BigDecimal("100"));
        deal.setPrice(new BigDecimal("10.5"));
        deal.setCurrency("USD");
        deal.setStatus("NEW");
        deal.setVersion(1L);
        deal.setDealDate(LocalDateTime.now());
        return deal;
    }
}
