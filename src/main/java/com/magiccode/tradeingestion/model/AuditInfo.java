package com.magiccode.tradeingestion.model;

import jakarta.persistence.Embeddable;
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
     * Username or identifier of the user who created the record
     */
    private String createdBy;
    
    /**
     * Date and time when the record was created
     */
    private LocalDateTime createdDate;
    
    /**
     * Username or identifier of the user who last modified the record
     */
    private String lastModifiedBy;
    
    /**
     * Date and time when the record was last modified
     */
    private LocalDateTime lastModifiedDate;
} 