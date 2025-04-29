package com.magiccode.tradeingestion.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a financial instrument in the system.
 * 
 * This class encapsulates basic information about a financial instrument,
 * including its unique identifier and trading symbol. It implements Serializable
 * to support caching and persistence operations.
 * 
 * @see Deal
 * @see FixedIncomeDerivativeDeal
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Instrument implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * Unique identifier for the instrument
     */
    private String id;
    
    /**
     * Trading symbol for the instrument
     */
    private String symbol;
} 