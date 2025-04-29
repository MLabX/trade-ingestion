package com.magiccode.tradeingestion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivacyPolicy {
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "privacy_pii_fields", 
            joinColumns = @JoinColumn(name = "deal_id"))
    @Column(name = "field_name")
    private Set<String> fieldsWithPII;
    
    @Column(name = "privacy_masking_policy")
    private String maskingPolicy;
    
    @Column(name = "privacy_retention_days")
    private Integer retentionPeriodDays;
} 