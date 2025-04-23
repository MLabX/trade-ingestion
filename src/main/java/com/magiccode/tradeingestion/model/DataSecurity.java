package com.magiccode.tradeingestion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents data security information for a financial deal.
 * 
 * This class encapsulates all security-related attributes for deal data,
 * including:
 * 1. Classification levels and access control
 * 2. Encryption information
 * 3. Data jurisdiction and retention policies
 * 
 * The class is designed to be embedded within a Deal entity
 * and provides a comprehensive set of security-related attributes
 * to ensure proper data protection and compliance.
 * 
 * @see Deal
 * @see FixedIncomeDerivativeDeal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class DataSecurity {
    /**
     * Security classification level of the data
     * (e.g., PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED)
     */
    @Column(name = "classification_level")
    private String classificationLevel;
    
    /**
     * Access control list defining who can access the data
     */
    @Column(name = "access_control_list")
    private String accessControlList;
    
    /**
     * Identifier of the encryption key used for the data
     */
    @Column(name = "encryption_key_id")
    private String encryptionKeyId;
    
    /**
     * Jurisdiction where the data is stored and processed
     */
    @Column(name = "data_jurisdiction")
    private String dataJurisdiction;
    
    /**
     * Policy defining how long the data should be retained
     */
    @Column(name = "retention_policy")
    private String retentionPolicy;
} 