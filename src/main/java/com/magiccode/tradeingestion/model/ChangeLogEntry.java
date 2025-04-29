package com.magiccode.tradeingestion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents an entry in the change log of a deal.
 * 
 * This class captures:
 * 1. The actor who made the change
 * 2. The timestamp of the change
 * 3. The type of change
 * 4. Any comments about the change
 * 
 * It is designed to be embedded within the AuditInfo class to provide
 * a comprehensive record of changes made to a deal.
 * 
 * @see AuditInfo
 * @see Actor
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ChangeLogEntry {
    
    /**
     * The actor who made the change
     */
    @Embedded
    private Actor changedBy;
    
    /**
     * The timestamp of the change
     */
    @Column(name = "change_timestamp")
    private LocalDateTime changeTimestamp;
    
    /**
     * The type of change
     */
    @Column(name = "change_type")
    private String changeType;
    
    /**
     * Any comments about the change
     */
    @Column(name = "comments")
    private String comments;
}