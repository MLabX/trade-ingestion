package com.magiccode.tradeingestion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegulatoryReporting {
    @Column(name = "regulatory_jurisdiction")
    private String reportingJurisdiction;
    
    @Column(name = "regulatory_action_type")
    private String actionType;
    
    @Column(name = "regulatory_uti")
    private String uti;
    
    @Column(name = "regulatory_entity_lei")
    private String reportingEntityLei;
    
    @Column(name = "regulatory_execution_timestamp")
    private LocalDateTime executionTimestamp;
    
    @Column(name = "regulatory_counterparty")
    private String reportingCounterparty;
} 