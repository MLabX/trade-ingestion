package com.magiccode.tradeingestion.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a counterparty in the financial system.
 * 
 * This class encapsulates basic information about a counterparty,
 * including their unique identifier and name. It implements Serializable
 * to support caching and persistence operations.
 * 
 * @see Deal
 * @see FixedIncomeDerivativeDeal
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Counterparty implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * Unique identifier for the counterparty
     */
    private String id;
    
    /**
     * Legal name of the counterparty
     */
    private String name;
} 