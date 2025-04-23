package com.magiccode.tradeingestion.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
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
    private Set<String> fieldsWithPII;
    
    private String maskingPolicy;
    private Integer retentionPeriodDays;
} 