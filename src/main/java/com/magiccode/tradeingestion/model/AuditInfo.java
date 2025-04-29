package com.magiccode.tradeingestion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents audit trail information for a financial deal.
 * 
 * This class encapsulates all audit-related attributes to track
 * the creation and modification history of deal data, including:
 * 1. User who created the record
 * 2. Creation timestamp
 * 3. User who last modified the record
 * 4. Last modification timestamp
 * 
 * The class is designed to be embedded within a Deal entity
 * and provides a comprehensive audit trail for compliance
 * and tracking purposes.
 * 
 * @see Deal
 * @see FixedIncomeDerivativeDeal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class AuditInfo {
    /**
     * User or system that created the record
     */
    @Embedded
    private Actor createdBy;

    /**
     * Date and time when the record was created
     */
    @Column(name = "audit_created_date")
    private LocalDateTime createdDate;

    /**
     * User or system that last modified the record
     */
    @Embedded
    private Actor lastModifiedBy;

    /**
     * Date and time when the record was last modified
     */
    @Column(name = "audit_last_modified_date")
    private LocalDateTime lastModifiedDate;
} 
