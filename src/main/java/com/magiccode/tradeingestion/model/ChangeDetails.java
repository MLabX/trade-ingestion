package com.magiccode.tradeingestion.model;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Represents the details of changes made during an amendment to a deal.
 * 
 * This class captures:
 * 1. The list of fields that were amended
 * 2. The old values of those fields
 * 3. The new values of those fields
 * 
 * It is designed to be embedded within a DealLifecycleEvent to provide
 * a comprehensive record of what changed during an amendment.
 * 
 * @see DealLifecycleEvent
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ChangeDetails {
    
    /**
     * List of field names that were amended
     */
    @ElementCollection
    @Column(name = "amended_field")
    private List<String> amendedFields;
    
    /**
     * Map of field names to their old values
     */
    @Lob
    @Column(name = "old_values")
    private String oldValuesJson;
    
    /**
     * Map of field names to their new values
     */
    @Lob
    @Column(name = "new_values")
    private String newValuesJson;
}