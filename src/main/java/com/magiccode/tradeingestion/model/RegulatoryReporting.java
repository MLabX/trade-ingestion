package com.magiccode.tradeingestion.model;

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
    private String reportingJurisdiction;
    private String actionType;
    private String uti;
    private String reportingEntityLei;
    private LocalDateTime executionTimestamp;
    private String reportingCounterparty;
} 