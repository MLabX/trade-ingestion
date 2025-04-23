package com.magiccode.tradeingestion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a leg of a financial derivative deal.
 * 
 * A deal leg is a component of a derivative contract that represents
 * one side of the cash flow exchange. This class supports:
 * 1. Fixed and floating rate legs
 * 2. Various rate types and conventions
 * 3. Notional amount management
 * 4. Payment frequency and business day conventions
 * 5. Rate caps and floors
 * 
 * The class is designed to be embedded within a Deal entity
 * and supports both fixed and floating rate instruments.
 * 
 * @see FixedIncomeDerivativeDeal
 * @see NotionalAmount
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class DealLeg {
    
    @Column(name = "leg_id")
    private String legId;
    
    @Column(name = "leg_type")
    private String legType;
    
    @Column(name = "leg_currency")
    private String legCurrency;
    
    @Column(name = "leg_notional")
    private BigDecimal legNotional;
    
    @Column(name = "leg_rate")
    private BigDecimal legRate;
    
    @Column(name = "leg_rate_type")
    private String rateType;
    
    @Column(name = "leg_start_date")
    private LocalDate legStartDate;
    
    @Column(name = "leg_end_date")
    private LocalDate legEndDate;
    
    @Column(name = "leg_day_count_convention")
    private String legDayCountConvention;
    
    @Column(name = "leg_business_day_convention")
    private String legBusinessDayConvention;
    
    @Column(name = "leg_payment_frequency")
    private String legPaymentFrequency;
    
    @Column(name = "leg_index")
    private String legIndex;
    
    @Column(name = "leg_spread")
    private BigDecimal legSpread;
    
    @Column(name = "leg_margin")
    private BigDecimal legMargin;
    
    @Column(name = "leg_cap_rate")
    private BigDecimal legCapRate;
    
    @Column(name = "leg_floor_rate")
    private BigDecimal legFloorRate;

    @Column(name = "leg_pay_or_receive")
    private String payOrReceive;

    /**
     * Inner class representing the notional amount of a deal leg.
     * This class encapsulates both the amount and currency of the notional.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotionalAmount {
        private BigDecimal amount;
        private String currency;
    }

    private NotionalAmount notionalAmount;

    /**
     * Sets the pay or receive indicator for this leg.
     * 
     * @param payOrReceive The direction of cash flow ("PAY" or "RECEIVE")
     * @return This DealLeg instance for method chaining
     */
    public DealLeg payOrReceive(String payOrReceive) {
        this.payOrReceive = payOrReceive;
        return this;
    }

    /**
     * Sets the rate type for this leg.
     * 
     * @param rateType The type of rate ("FIXED" or "FLOATING")
     * @return This DealLeg instance for method chaining
     */
    public DealLeg rateType(String rateType) {
        this.legType = rateType;
        return this;
    }

    /**
     * Configures this leg as a fixed rate leg with the specified rate.
     * 
     * @param rate The fixed rate to be applied
     * @return This DealLeg instance for method chaining
     */
    public DealLeg fixedRate(BigDecimal rate) {
        this.legRate = rate;
        this.rateType = "FIXED";
        return this;
    }

    /**
     * Configures this leg as a floating rate leg with the specified index.
     * 
     * @param index The floating rate index to be used
     * @return This DealLeg instance for method chaining
     */
    public DealLeg floatingRateIndex(String index) {
        this.legIndex = index;
        this.rateType = "FLOATING";
        return this;
    }
} 