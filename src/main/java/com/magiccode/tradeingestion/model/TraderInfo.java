package com.magiccode.tradeingestion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents trader information associated with a financial deal.
 * 
 * This class encapsulates all relevant information about the trader
 * who executed or is responsible for a deal, including:
 * 1. Personal identification and contact details
 * 2. Organizational information (desk, department, location)
 * 3. Role and status information
 * 4. Manager and hire date details
 * 5. Additional notes and comments
 * 
 * The class is designed to be embedded within a Deal entity
 * and provides a comprehensive set of trader-related attributes.
 * 
 * @see FixedIncomeDerivativeDeal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class TraderInfo {
    /**
     * Unique identifier for the trader
     */
    @Column(name = "trader_id")
    private String id;
    
    /**
     * Full name of the trader
     */
    @Column(name = "trader_name")
    private String name;
    
    /**
     * Trading desk the trader belongs to
     */
    @Column(name = "trader_desk")
    private String desk;
    
    /**
     * Email address of the trader
     */
    @Column(name = "trader_email")
    private String email;
    
    /**
     * Contact phone number of the trader
     */
    @Column(name = "trader_phone")
    private String phone;
    
    /**
     * Department the trader belongs to
     */
    @Column(name = "trader_department")
    private String department;
    
    /**
     * Physical location of the trader
     */
    @Column(name = "trader_location")
    private String location;
    
    /**
     * Current status of the trader (e.g., ACTIVE, INACTIVE)
     */
    @Column(name = "trader_status")
    private String status;
    
    /**
     * Role of the trader within the organization
     */
    @Column(name = "trader_role")
    private String role;
    
    /**
     * Name of the trader's manager
     */
    @Column(name = "trader_manager")
    private String manager;
    
    /**
     * Date when the trader was hired
     */
    @Column(name = "trader_hire_date")
    private String hireDate;
    
    /**
     * Additional notes or comments about the trader
     */
    @Column(name = "trader_notes")
    private String notes;

    /**
     * Gets the trader's unique identifier.
     * 
     * @return The trader's ID
     */
    public String id() {
        return this.id;
    }
} 