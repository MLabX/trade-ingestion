package com.magiccode.tradeingestion.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents valuation information for a financial deal.
 * 
 * This class encapsulates all valuation-related attributes to track
 * how a deal was valued, including:
 * 1. Valuation model used
 * 2. Price source information
 * 3. Valuation timestamp
 * 4. Valuation currency
 * 
 * The class is designed to be embedded within a Deal entity
 * and provides a comprehensive view of the deal's valuation
 * process and parameters.
 * 
 * @see Deal
 * @see FixedIncomeDerivativeDeal
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValuationInfo {
    /**
     * Identifier of the valuation model used
     */
    @Column(name = "valuation_model_id")
    private String modelId;
    
    /**
     * Source of the price data used in valuation
     */
    @Column(name = "valuation_price_source")
    private String priceSource;
    
    /**
     * Timestamp when the valuation was performed
     */
    @Column(name = "valuation_timestamp")
    private LocalDateTime valuationTimestamp;
    
    /**
     * Currency in which the valuation was performed
     */
    @Column(name = "valuation_currency")
    private String valuationCurrency;
} 