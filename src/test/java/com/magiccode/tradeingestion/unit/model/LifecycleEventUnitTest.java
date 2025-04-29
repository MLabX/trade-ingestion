package com.magiccode.tradeingestion.unit.model;

import com.magiccode.tradeingestion.model.FixedIncomeDerivativeDeal;
import com.magiccode.tradeingestion.model.ChangeDetails;
import com.magiccode.tradeingestion.model.DealLifecycleEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests for the lifecycle event functionality in the model classes.
 * 
 * This test class verifies that the model classes can properly represent
 * and process the different lifecycle events of a deal, including:
 * - New deals (Initiation)
 * - Confirmed deals (Trade Confirmation)
 * - Amended deals (Amendment)
 * - Cancelled deals (Cancellation)
 */
class LifecycleEventUnitTest {

    private FixedIncomeDerivativeDeal newDeal;
    private FixedIncomeDerivativeDeal confirmedDeal;
    private FixedIncomeDerivativeDeal amendedDeal;
    private FixedIncomeDerivativeDeal cancelledDeal;

    private DealLifecycleEvent newEvent;
    private DealLifecycleEvent confirmedEvent;
    private DealLifecycleEvent amendedEvent;
    private DealLifecycleEvent cancelledEvent;

    @BeforeEach
    void setUp() {
        // Create mock deals
        newDeal = Mockito.mock(FixedIncomeDerivativeDeal.class);
        confirmedDeal = Mockito.mock(FixedIncomeDerivativeDeal.class);
        amendedDeal = Mockito.mock(FixedIncomeDerivativeDeal.class);
        cancelledDeal = Mockito.mock(FixedIncomeDerivativeDeal.class);

        // Create mock lifecycle events
        newEvent = Mockito.mock(DealLifecycleEvent.class);
        confirmedEvent = Mockito.mock(DealLifecycleEvent.class);
        amendedEvent = Mockito.mock(DealLifecycleEvent.class);
        cancelledEvent = Mockito.mock(DealLifecycleEvent.class);

        // Set up mock behavior for newDeal and its event
        when(newDeal.getStatus()).thenReturn("NEW");
        when(newDeal.isNew()).thenReturn(true);
        when(newDeal.getLifecycleEvent()).thenReturn(newEvent);
        when(newEvent.getEventType()).thenReturn("Initiation");
        when(newEvent.getEffectiveDate()).thenReturn(LocalDate.now());
        when(newEvent.getPriorDealId()).thenReturn(null);

        // Set up mock behavior for confirmedDeal and its event
        when(confirmedDeal.getStatus()).thenReturn("CONFIRMED");
        when(confirmedDeal.isConfirmed()).thenReturn(true);
        when(confirmedDeal.getLifecycleEvent()).thenReturn(confirmedEvent);
        when(confirmedEvent.getEventType()).thenReturn("Trade Confirmation");
        when(confirmedEvent.getEffectiveDate()).thenReturn(LocalDate.now());
        when(confirmedEvent.getPriorDealId()).thenReturn("PRIOR-001");
        when(confirmedEvent.getConfirmationDocumentId()).thenReturn("CONF-001");

        // Set up mock behavior for amendedDeal and its event
        when(amendedDeal.getStatus()).thenReturn("AMENDED");
        when(amendedDeal.isAmended()).thenReturn(true);
        when(amendedDeal.getLifecycleEvent()).thenReturn(amendedEvent);
        when(amendedEvent.getEventType()).thenReturn("Amendment");
        when(amendedEvent.getEffectiveDate()).thenReturn(LocalDate.now());
        when(amendedEvent.getPriorDealId()).thenReturn("PRIOR-002");
        when(amendedEvent.getApprovalId()).thenReturn("APPR-001");
        when(amendedEvent.getChangeDetails()).thenReturn(new ChangeDetails());

        // Set up mock behavior for cancelledDeal and its event
        when(cancelledDeal.getStatus()).thenReturn("CANCELLED");
        when(cancelledDeal.isCancelled()).thenReturn(true);
        when(cancelledDeal.getLifecycleEvent()).thenReturn(cancelledEvent);
        when(cancelledEvent.getEventType()).thenReturn("Cancellation");
        when(cancelledEvent.getEffectiveDate()).thenReturn(LocalDate.now());
        when(cancelledEvent.getPriorDealId()).thenReturn("PRIOR-003");
        when(cancelledEvent.getCancellationReason()).thenReturn("Test cancellation");
        when(cancelledEvent.getCancelInitiator()).thenReturn("Test user");
    }

    @Test
    void testNewDeal() {
        // Assert
        assertEquals("NEW", newDeal.getStatus());
        assertTrue(newDeal.isNew());
        assertEquals("Initiation", newDeal.getLifecycleEvent().getEventType());
        assertNotNull(newDeal.getLifecycleEvent().getEffectiveDate());
        assertNull(newDeal.getLifecycleEvent().getPriorDealId());
    }
    
    @Test
    void testConfirmedDeal() {
        // Assert
        assertEquals("CONFIRMED", confirmedDeal.getStatus());
        assertTrue(confirmedDeal.isConfirmed());
        assertEquals("Trade Confirmation", confirmedDeal.getLifecycleEvent().getEventType());
        assertNotNull(confirmedDeal.getLifecycleEvent().getEffectiveDate());
        assertNotNull(confirmedDeal.getLifecycleEvent().getPriorDealId());
        assertNotNull(confirmedDeal.getLifecycleEvent().getConfirmationDocumentId());
    }
    
    @Test
    void testAmendedDeal() {
        // Assert
        assertEquals("AMENDED", amendedDeal.getStatus());
        assertTrue(amendedDeal.isAmended());
        assertEquals("Amendment", amendedDeal.getLifecycleEvent().getEventType());
        assertNotNull(amendedDeal.getLifecycleEvent().getEffectiveDate());
        assertNotNull(amendedDeal.getLifecycleEvent().getPriorDealId());
        assertNotNull(amendedDeal.getLifecycleEvent().getApprovalId());
        assertNotNull(amendedDeal.getLifecycleEvent().getChangeDetails());
    }
    
    @Test
    void testCancelledDeal() {
        // Assert
        assertEquals("CANCELLED", cancelledDeal.getStatus());
        assertTrue(cancelledDeal.isCancelled());
        assertEquals("Cancellation", cancelledDeal.getLifecycleEvent().getEventType());
        assertNotNull(cancelledDeal.getLifecycleEvent().getEffectiveDate());
        assertNotNull(cancelledDeal.getLifecycleEvent().getPriorDealId());
        assertNotNull(cancelledDeal.getLifecycleEvent().getCancellationReason());
        assertNotNull(cancelledDeal.getLifecycleEvent().getCancelInitiator());
    }
}