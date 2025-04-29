package com.magiccode.tradeingestion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an actor (user or system) that performs actions on deals.
 * 
 * This class captures:
 * 1. The ID of the actor
 * 2. The type of the actor (HUMAN or SYSTEM)
 * 
 * It is designed to be embedded within audit-related classes to provide
 * a comprehensive record of who performed actions on deals.
 * 
 * @see AuditInfo
 * @see ChangeLogEntry
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Actor {
    
    /**
     * ID of the actor
     */
    @Column(name = "actor_id")
    private String actorId;
    
    /**
     * Type of the actor (HUMAN or SYSTEM)
     */
    @Column(name = "actor_type")
    private String actorType;
}