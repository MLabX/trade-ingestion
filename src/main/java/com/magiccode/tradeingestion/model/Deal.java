package com.magiccode.tradeingestion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Deal ID is required")
    @Column(name = "deal_id", unique = true, nullable = false)
    private String dealId;

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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Custom method to increment version for optimistic locking
    public Deal withIncrementedVersion() {
        return new Deal(
            id, dealId, clientId, instrumentId, quantity, price, currency,
            status, version + 1, dealDate, createdAt, updatedAt, processedAt
        );
    }

    // Factory method for creating a new deal
    public static Deal createNew(
        String dealId,
        String clientId,
        String instrumentId,
        BigDecimal quantity,
        BigDecimal price,
        String currency
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new Deal(
            null,
            dealId,
            clientId,
            instrumentId,
            quantity,
            price,
            currency,
            "NEW",
            1L,
            now,
            now,
            now,
            null
        );
    }
    
    // Java 21 pattern matching for instanceof
    public boolean isNew() {
        return "NEW".equals(status);
    }
    
    public boolean isProcessing() {
        return "PROCESSING".equals(status);
    }
    
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
    
    public boolean isFailed() {
        return "FAILED".equals(status);
    }
}