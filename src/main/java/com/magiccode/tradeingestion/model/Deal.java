package com.magiccode.tradeingestion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.magiccode.tradeingestion.exception.DealProcessingException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.io.Serializable;

/**
 * Base abstract class representing a financial deal in the system.
 * 
 * This class serves as the foundation for all deal types in the system and provides:
 * 1. Common deal attributes and validation
 * 2. Audit and security information
 * 3. Lifecycle management
 * 4. Processing metadata
 * 
 * The class uses JPA annotations for persistence and validation constraints
 * to ensure data integrity. It implements optimistic locking through versioning
 * to handle concurrent updates.
 * 
 * Key features:
 * - UUID-based primary key
 * - Required fields with validation
 * - Embedded audit and security information
 * - Automatic timestamp management
 * - Status tracking
 * - Notional amount calculation
 * 
 * @see FixedIncomeDerivativeDeal
 * @see DealLeg
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Entity
@Table(name = "deals")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Deal implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Deal ID is required")
    @Column(name = "base_deal_id", unique = true, nullable = false)
    private String dealId;

    @NotBlank(message = "Event type is required")
    @Pattern(regexp = "^(CREATED|UPDATED|CANCELLED)$", message = "Invalid event type")
    @Column(name = "base_event_type", nullable = false)
    private String eventType;

    @NotBlank(message = "Client ID is required")
    @Column(name = "base_client_id", nullable = false)
    private String clientId;

    @NotBlank(message = "Instrument ID is required")
    @Column(name = "base_instrument_id", nullable = false)
    private String instrumentId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Column(name = "base_quantity", nullable = false)
    private BigDecimal quantity;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Column(name = "base_price", nullable = false)
    private BigDecimal price;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter code")
    @Column(name = "base_currency", nullable = false)
    private String currency;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(NEW|PROCESSING|COMPLETED|FAILED|CONFIRMED|AMENDED|CANCELLED)$", message = "Invalid status")
    @Column(name = "base_status", nullable = false)
    private String status;

    @Version
    @Column(name = "base_version", nullable = false)
    private Long version;

    @NotNull(message = "Deal date is required")
    @Column(name = "base_deal_date", nullable = false)
    private LocalDateTime dealDate;

    @CreationTimestamp
    @Column(name = "base_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "base_updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "base_processed_at")
    private LocalDateTime processedAt;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "createdBy", column = @Column(name = "base_created_by")),
        @AttributeOverride(name = "createdDate", column = @Column(name = "base_created_date")),
        @AttributeOverride(name = "lastModifiedBy", column = @Column(name = "base_last_modified_by")),
        @AttributeOverride(name = "lastModifiedDate", column = @Column(name = "base_last_modified_date"))
    })
    private AuditInfo audit;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "classificationLevel", column = @Column(name = "base_classification_level")),
        @AttributeOverride(name = "accessControlList", column = @Column(name = "base_access_control_list")),
        @AttributeOverride(name = "encryptionKeyId", column = @Column(name = "base_encryption_key_id")),
        @AttributeOverride(name = "dataJurisdiction", column = @Column(name = "base_data_jurisdiction")),
        @AttributeOverride(name = "retentionPolicy", column = @Column(name = "base_retention_policy"))
    })
    private DataSecurity dataSecurity;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "processingStatus", column = @Column(name = "base_processing_status")),
        @AttributeOverride(name = "processingStartTime", column = @Column(name = "base_processing_start_time")),
        @AttributeOverride(name = "processingEndTime", column = @Column(name = "base_processing_end_time")),
        @AttributeOverride(name = "processingErrors", column = @Column(name = "base_processing_errors")),
        @AttributeOverride(name = "retryCount", column = @Column(name = "base_processing_retry_count"))
    })
    private ProcessingMetadata processingMetadata;

    /**
     * Calculates the notional amount of the deal.
     * The notional amount is the product of quantity and price.
     * 
     * @return The calculated notional amount
     */
    public BigDecimal getNotionalAmount() {
        return quantity.multiply(price);
    }

    /**
     * Gets the instrument quantity.
     * This is a convenience method that returns the deal's quantity.
     * 
     * @return The instrument quantity
     */
    public BigDecimal getInstrumentQuantity() {
        return quantity;
    }

    /**
     * Gets the instrument price.
     * This is a convenience method that returns the deal's price.
     * 
     * @return The instrument price
     */
    public BigDecimal getInstrumentPrice() {
        return price;
    }

    /**
     * Gets the instrument settlement amount.
     * This is a convenience method that returns the notional amount.
     * 
     * @return The instrument settlement amount
     */
    public BigDecimal getInstrumentSettlementAmount() {
        return getNotionalAmount();
    }

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
     * Increments the version number for optimistic locking.
     * This method should be called before saving an updated deal
     * to handle concurrent modifications.
     * 
     * @return The deal instance with incremented version
     */
    public Deal withIncrementedVersion() {
        this.version = version + 1;
        return this;
    }

    /**
     * Factory method for creating a new deal instance.
     * This method provides a type-safe way to create deal instances
     * with common initialization logic.
     * 
     * @param <T> The type of deal to create
     * @param dealClass The class of the deal to create
     * @param dealId The unique identifier for the deal
     * @param clientId The client identifier
     * @param instrumentId The instrument identifier
     * @param quantity The deal quantity
     * @param price The deal price
     * @param currency The deal currency (3-letter code)
     * @return A new deal instance
     * @throws DealProcessingException if the deal cannot be created
     */
    public static <T extends Deal> T createNew(
        Class<T> dealClass,
        String dealId,
        String clientId,
        String instrumentId,
        BigDecimal quantity,
        BigDecimal price,
        String currency
    ) {
        try {
            T deal = dealClass.getDeclaredConstructor().newInstance();
            deal.setDealId(dealId);
            deal.setClientId(clientId);
            deal.setInstrumentId(instrumentId);
            deal.setQuantity(quantity);
            deal.setPrice(price);
            deal.setCurrency(currency);
            deal.setStatus("NEW");
            deal.setVersion(1L);
            deal.setDealDate(LocalDateTime.now());
            deal.setCreatedAt(LocalDateTime.now());
            deal.setUpdatedAt(LocalDateTime.now());
            return deal;
        } catch (Exception e) {
            throw new DealProcessingException("Failed to create new deal instance", e);
        }
    }

    /**
     * Checks if the deal is in NEW status.
     * 
     * @return true if the deal status is NEW
     */
    public boolean isNew() {
        return "NEW".equalsIgnoreCase(status);
    }

    /**
     * Checks if the deal is in PROCESSING status.
     * 
     * @return true if the deal status is PROCESSING
     */
    public boolean isProcessing() {
        return "PROCESSING".equalsIgnoreCase(status);
    }

    /**
     * Checks if the deal is in COMPLETED status.
     * 
     * @return true if the deal status is COMPLETED
     */
    public boolean isCompleted() {
        return "COMPLETED".equalsIgnoreCase(status);
    }

    /**
     * Checks if the deal is in FAILED status.
     * 
     * @return true if the deal status is FAILED
     */
    public boolean isFailed() {
        return "FAILED".equalsIgnoreCase(status);
    }

    /**
     * Checks if the deal is in CONFIRMED status.
     * 
     * @return true if the deal status is CONFIRMED
     */
    public boolean isConfirmed() {
        return "CONFIRMED".equalsIgnoreCase(status);
    }

    /**
     * Checks if the deal is in AMENDED status.
     * 
     * @return true if the deal status is AMENDED
     */
    public boolean isAmended() {
        return "AMENDED".equalsIgnoreCase(status);
    }

    /**
     * Checks if the deal is in CANCELLED status.
     * 
     * @return true if the deal status is CANCELLED
     */
    public boolean isCancelled() {
        return "CANCELLED".equalsIgnoreCase(status);
    }

    public abstract String getDealType();
}
