package com.magiccode.tradeingestion.model;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class TestDeal extends Deal {
    // No additional fields needed as we're just testing the base Deal functionality
    
    @Override
    public String getDealType() {
        return "TEST";
    }
} 