package com.magiccode.tradeingestion.model;

import jakarta.persistence.Embeddable;
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
    private String calypsoTradeId;
    private String blotterId;
    private Map<String, String> externalSystemRefs;
} 