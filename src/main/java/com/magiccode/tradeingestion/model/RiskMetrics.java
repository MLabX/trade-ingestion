package com.magiccode.tradeingestion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
@NoArgsConstructor
@AllArgsConstructor
public class RiskMetrics {
    /**
     * Inner class representing a risk metric value with its associated
     * currency and precision information.
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static abstract class MetricValue {
        /**
         * Numerical value of the metric
         */
        @Column(name = "value", insertable = false, updatable = false)
        private BigDecimal value;
        
        @Column(name = "currency", insertable = false, updatable = false)
        private String currency;
        
        /**
         * Notes about the precision or calculation methodology
         */
        @Column(name = "precision_note", insertable = false, updatable = false)
        private String precisionNote;
    }

    @Embeddable
    @Data
    @EqualsAndHashCode(callSuper = false)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NpvMetricValue extends MetricValue {
        @Column(name = "npv_value", insertable = false, updatable = false)
        private BigDecimal value;
        
        @Column(name = "npv_currency", insertable = false, updatable = false)
        private String currency;
        
        @Column(name = "npv_precision_note", insertable = false, updatable = false)
        private String precisionNote;
    }

    @Embeddable
    @Data
    @EqualsAndHashCode(callSuper = false)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pv01MetricValue extends MetricValue {
        @Column(name = "pv01_value", insertable = false, updatable = false)
        private BigDecimal value;
        
        @Column(name = "pv01_currency", insertable = false, updatable = false)
        private String currency;
        
        @Column(name = "pv01_precision_note", insertable = false, updatable = false)
        private String precisionNote;
    }

    @Embeddable
    @Data
    @EqualsAndHashCode(callSuper = false)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dv01MetricValue extends MetricValue {
        @Column(name = "dv01_value", insertable = false, updatable = false)
        private BigDecimal value;
        
        @Column(name = "dv01_currency", insertable = false, updatable = false)
        private String currency;
        
        @Column(name = "dv01_precision_note", insertable = false, updatable = false)
        private String precisionNote;
    }

    /**
     * Net Present Value of the deal
     */
    private NpvMetricValue npv;
    
    /**
     * Price Value of a Basis Point (PV01) of the deal
     */
    private Pv01MetricValue pv01;
    
    /**
     * Dollar Value of a Basis Point (DV01) of the deal
     */
    private Dv01MetricValue dv01;
} 