package com.magiccode.tradeingestion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a leg of a financial derivative deal.
 * Each leg can be either a fixed or floating rate component.
 */
@Entity
@Table(name = "deal_leg")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DealLeg implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "derivative_deal_id", nullable = false)
    private FixedIncomeDerivativeDeal deal;

    @NotBlank(message = "Leg ID is required")
    @Column(name = "leg_id", nullable = false)
    private String legId;

    @NotBlank(message = "Leg type is required")
    @Column(name = "leg_type", nullable = false)
    private String legType;

    @NotBlank(message = "Pay or receive is required")
    @Column(name = "pay_or_receive", nullable = false)
    private String payOrReceive;

    @NotBlank(message = "Rate type is required")
    @Column(name = "rate_type", nullable = false)
    private String rateType;

    @NotBlank(message = "Leg currency is required")
    @Column(name = "leg_currency", nullable = false)
    private String legCurrency;

    @Embedded
    private NotionalAmount notionalAmount;

    @Column(name = "fixed_rate", precision = 10, scale = 6)
    private BigDecimal fixedRate;

    @Column(name = "floating_rate_index")
    private String floatingRateIndex;

    @Column(name = "floating_rate_spread", precision = 10, scale = 6)
    private BigDecimal floatingRateSpread;

    @Column(name = "payment_frequency")
    private String paymentFrequency;

    @Column(name = "day_count_convention")
    private String dayCountConvention;

    @Column(name = "business_day_convention")
    private String businessDayConvention;

    @Column(name = "version", nullable = false)
    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Configures this leg as a fixed rate leg.
     */
    public void configureAsFixedRateLeg(BigDecimal fixedRate) {
        this.rateType = "FIXED";
        this.fixedRate = fixedRate;
        this.floatingRateIndex = null;
        this.floatingRateSpread = null;
    }

    /**
     * Configures this leg as a floating rate leg.
     */
    public void configureAsFloatingRateLeg(String floatingRateIndex, BigDecimal floatingRateSpread) {
        this.rateType = "FLOATING";
        this.fixedRate = null;
        this.floatingRateIndex = floatingRateIndex;
        this.floatingRateSpread = floatingRateSpread;
    }
} 