package com.magiccode.tradeingestion.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalReferences {
    @Column(name = "external_calypso_trade_id")
    private String calypsoTradeId;
    
    @Column(name = "external_blotter_id")
    private String blotterId;
    
    @ElementCollection
    @CollectionTable(name = "external_system_refs",
            joinColumns = @JoinColumn(name = "deal_id"))
    @MapKeyColumn(name = "system_name")
    @Column(name = "reference_id")
    private Map<String, String> externalSystemRefs;
} 