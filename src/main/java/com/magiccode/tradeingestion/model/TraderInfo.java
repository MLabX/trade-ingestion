package com.magiccode.tradeingestion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents trader information associated with a financial deal.
 * Contains the trader's identification, name, and desk information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class TraderInfo {
    /**
     * Unique identifier for the trader
     */
    @Column(name = "trader_id")
    private String id;
    
    /**
     * Full name of the trader
     */
    @Column(name = "trader_name")
    private String name;
    
    /**
     * Trading desk the trader belongs to
     */
    @Column(name = "trader_desk")
    private String desk;
} 