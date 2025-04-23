package com.magiccode.tradeingestion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.magiccode.tradeingestion.exception.DealProcessingException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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
public abstract class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Deal ID is required")
    @Column(name = "deal_id", unique = true, nullable = false)
    private String dealId;

    @NotBlank(message = "Event type is required")
    @Pattern(regexp = "^(CREATED|UPDATED|CANCELLED)$", message = "Invalid event type")
    @Column(name = "event_type", nullable = false)
    private String eventType;

    @NotBlank(message = "Client ID is required")
    @Column(name = "client_id", nullable = false)
    private String clientId;

    @NotBlank(message = "Instrument ID is required")
    @Column(name = "instrument_id", nullable = false)
    private String instrumentId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter code")
    @Column(name = "currency", nullable = false)
    private String currency;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(NEW|PROCESSING|COMPLETED|FAILED)$", message = "Invalid status")
    @Column(name = "status", nullable = false)
    private String status;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @NotNull(message = "Deal date is required")
    @Column(name = "deal_date", nullable = false)
    private LocalDateTime dealDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Embedded
    private AuditInfo audit;

    @Embedded
    private DataSecurity dataSecurity;

    @Embedded
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
        return "NEW".equals(status);
    }
    
    /**
     * Checks if the deal is in PROCESSING status.
     * 
     * @return true if the deal status is PROCESSING
     */
    public boolean isProcessing() {
        return "PROCESSING".equals(status);
    }
    
    /**
     * Checks if the deal is in COMPLETED status.
     * 
     * @return true if the deal status is COMPLETED
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
    
    /**
     * Checks if the deal is in FAILED status.
     * 
     * @return true if the deal status is FAILED
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    public abstract String getDealType();
}