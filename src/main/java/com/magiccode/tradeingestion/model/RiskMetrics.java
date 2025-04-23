package com.magiccode.tradeingestion.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents risk metrics for a financial deal.
 * 
 * This class encapsulates all risk-related calculations and metrics
 * for a deal, including:
 * 1. Net Present Value (NPV)
 * 2. Price Value of a Basis Point (PV01)
 * 3. Dollar Value of a Basis Point (DV01)
 * 
 * Each metric includes its value, currency, and precision information
 * to ensure accurate risk reporting and analysis.
 * 
 * The class is designed to be embedded within a Deal entity
 * and provides a comprehensive view of the deal's risk profile.
 * 
 * @see Deal
 * @see FixedIncomeDerivativeDeal
 * @see MetricValue
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskMetrics {
    /**
     * Net Present Value of the deal
     */
    private MetricValue npv;
    
    /**
     * Price Value of a Basis Point (PV01) of the deal
     */
    private MetricValue pv01;
    
    /**
     * Dollar Value of a Basis Point (DV01) of the deal
     */
    private MetricValue dv01;
    
    /**
     * Inner class representing a risk metric value with its associated
     * currency and precision information.
     */
    @Embeddable
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricValue {
        /**
         * Numerical value of the metric
         */
        private BigDecimal value;
        
        /**
         * Currency in which the value is expressed
         */
        private String currency;
        
        /**
         * Notes about the precision or calculation methodology
         */
        private String precisionNote;
    }
} 