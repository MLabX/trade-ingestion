package com.magiccode.tradeingestion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.catalina.LifecycleEvent;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Represents a Fixed Income Derivative Deal in the system.
 * 
 * This class extends the base Deal class and adds specific attributes
 * and behaviors for fixed income derivative instruments. It includes:
 * 1. Deal-specific dates (trade, value, maturity)
 * 2. Booking and trader information
 * 3. Counterparty details
 * 4. Deal legs for complex instruments
 * 5. Valuation and risk metrics
 * 6. Regulatory reporting requirements
 * 
 * The class implements JPA entity mapping with:
 * - UUID-based primary key
 * - Required field validation
 * - Embedded components for complex data
 * - Version control for optimistic locking
 * - Automatic timestamp management
 * 
 * @see Deal
 * @see DealLeg
 * @see BookingInfo
 * @see TraderInfo
 * @see CounterpartyInfo
 */
@Entity
@Table(name = "fixed_income_derivative_deals")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class FixedIncomeDerivativeDeal extends Deal implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Deal ID is required")
    @Column(name = "deal_id", unique = true, nullable = false)
    private String dealId;

    @NotBlank(message = "Deal type is required")
    @Column(name = "deal_type", nullable = false)
    private String dealType;

    @NotBlank(message = "Execution venue is required")
    @Column(name = "execution_venue", nullable = false)
    private String executionVenue;

    @NotNull(message = "Trade date is required")
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @NotNull(message = "Value date is required")
    @Column(name = "value_date", nullable = false)
    private LocalDate valueDate;

    @NotNull(message = "Maturity date is required")
    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;

    @NotBlank(message = "Status is required")
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "is_back_dated", nullable = false)
    private boolean isBackDated;

    @Embedded
    private BookingInfo bookingInfo;

    @Embedded
    private TraderInfo trader;

    @Embedded
    private CounterpartyInfo counterparty;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "deal_id", referencedColumnName = "deal_id")
    private List<DealLeg> legs;

    @Embedded
    private ValuationInfo valuation;

    @Embedded
    private RiskMetrics riskMetrics;

    @Embedded
    private RegulatoryReporting regulatoryReporting;

    @Embedded
    private LifecycleEvent lifecycleEvent;

    @Embedded
    private ExternalReferences externalReferences;

    @Embedded
    private AuditInfo audit;

    @Embedded
    private DataSecurity dataSecurity;

    @Embedded
    private PrivacyPolicy privacyPolicy;

    @Embedded
    private ProcessingMetadata processingMetadata;

    @Column(name = "version", nullable = false)
    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * JPA callback method executed before persisting a new entity.
     * Sets the creation and update timestamps.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * JPA callback method executed before updating an entity.
     * Updates the modification timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Factory method for creating a new Fixed Income Derivative Deal.
     * This method provides a type-safe way to create deal instances
     * with all required fields and proper initialization.
     * 
     * @param dealId The unique identifier for the deal
     * @param dealType The type of derivative deal
     * @param executionVenue The venue where the deal was executed
     * @param tradeDate The date when the deal was traded
     * @param valueDate The date when the deal becomes effective
     * @param maturityDate The date when the deal matures
     * @param status The current status of the deal
     * @param isBackDated Whether the deal is back-dated
     * @param bookingInfo The booking information for the deal
     * @param trader The trader information
     * @param counterparty The counterparty information
     * @param legs The list of deal legs
     * @return A new FixedIncomeDerivativeDeal instance
     */
    public static FixedIncomeDerivativeDeal createNew(
        String dealId,
        String dealType,
        String executionVenue,
        LocalDate tradeDate,
        LocalDate valueDate,
        LocalDate maturityDate,
        String status,
        boolean isBackDated,
        BookingInfo bookingInfo,
        TraderInfo trader,
        CounterpartyInfo counterparty,
        List<DealLeg> legs
    ) {
        return FixedIncomeDerivativeDeal.builder()
            .dealId(dealId)
            .dealType(dealType)
            .executionVenue(executionVenue)
            .tradeDate(tradeDate)
            .valueDate(valueDate)
            .maturityDate(maturityDate)
            .status(status)
            .isBackDated(isBackDated)
            .bookingInfo(bookingInfo)
            .trader(trader)
            .counterparty(counterparty)
            .legs(legs)
            .version(1L)
            .build();
    }

    @Override
    public String getDealType() {
        return dealType;
    }
} 