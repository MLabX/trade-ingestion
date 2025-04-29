package com.magiccode.tradeingestion.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents lifecycle events for a financial deal.
 * 
 * This class tracks important events in the lifecycle of a deal,
 * including:
 * 1. Event type (e.g., Initiation, Trade Confirmation, Amendment, Cancellation)
 * 2. Effective date of the event
 * 3. Prior deal ID for reference
 * 4. Event-specific details like confirmation document, approval, change details, etc.
 * 
 * The class is designed to be embedded within a Deal entity
 * and provides a comprehensive view of the deal's lifecycle.
 * 
 * @see Deal
 * @see FixedIncomeDerivativeDeal
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealLifecycleEvent {
    /**
     * Type of the lifecycle event (Initiation, Trade Confirmation, Amendment, Cancellation)
     */
    @Column(name = "lifecycle_event_type")
    private String eventType;

    /**
     * Effective date of the event
     */
    @Column(name = "lifecycle_effective_date")
    private LocalDate effectiveDate;

    /**
     * ID of the prior deal version
     */
    @Column(name = "lifecycle_prior_deal_id")
    private String priorDealId;

    /**
     * Confirmation document ID (for Trade Confirmation events)
     */
    @Column(name = "lifecycle_confirmation_document_id")
    private String confirmationDocumentId;

    /**
     * Approval ID (for Amendment events)
     */
    @Column(name = "lifecycle_approval_id")
    private String approvalId;

    /**
     * Reason for cancellation (for Cancellation events)
     */
    @Column(name = "lifecycle_cancellation_reason")
    private String cancellationReason;

    /**
     * Initiator of the cancellation (for Cancellation events)
     */
    @Column(name = "lifecycle_cancel_initiator")
    private String cancelInitiator;

    /**
     * Change details (for Amendment events)
     */
    @Embedded
    private ChangeDetails changeDetails;

    /**
     * Timestamp when the event occurred
     */
    @Column(name = "lifecycle_event_timestamp")
    private LocalDateTime eventTimestamp;

    /**
     * Description of the event
     */
    @Column(name = "lifecycle_event_description")
    private String eventDescription;

    /**
     * User who triggered the event
     */
    @Column(name = "lifecycle_event_user")
    private String eventUser;
}
