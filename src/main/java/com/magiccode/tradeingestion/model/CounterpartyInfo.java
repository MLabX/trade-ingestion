package com.magiccode.tradeingestion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents counterparty information for a financial deal.
 * 
 * This class encapsulates all relevant information about the counterparty
 * involved in a deal, including:
 * 1. Legal entity identification
 * 2. Regulatory identifiers (LEI)
 * 3. Jurisdictional information
 * 4. Confidentiality and data retention policies
 * 
 * The class is designed to be embedded within a Deal entity
 * and provides a comprehensive set of counterparty-related attributes.
 * 
 * @see FixedIncomeDerivativeDeal
 * @see ConfidentialityPolicy
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CounterpartyInfo {
    /**
     * Unique identifier for the counterparty entity
     */
    @Column(name = "counterparty_entity_id")
    private String entityId;
    
    /**
     * Legal name of the counterparty
     */
    @Column(name = "counterparty_legal_name")
    private String legalName;
    
    /**
     * Legal Entity Identifier (LEI) of the counterparty
     */
    @Column(name = "counterparty_lei")
    private String lei;
    
    /**
     * Jurisdiction where the counterparty is registered
     */
    @Column(name = "counterparty_jurisdiction")
    private String jurisdiction;
    
    /**
     * Inner class representing confidentiality and data retention policies
     * for the counterparty's information.
     */
    @Embeddable
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfidentialityPolicy {
        /**
         * Indicates whether the counterparty's information requires masking
         */
        @Column(name = "counterparty_requires_masking")
        private boolean requiresMasking;
        
        /**
         * Number of days the counterparty's information should be retained
         */
        @Column(name = "counterparty_retention_days")
        private int retentionDays;
    }
    
    /**
     * Confidentiality and data retention policies for this counterparty
     */
    @Column(name = "counterparty_confidentiality_policy")
    private ConfidentialityPolicy confidentialityPolicy;
} 