package com.magiccode.tradeingestion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents booking information for a financial deal.
 * 
 * This class encapsulates all the necessary information for booking a deal,
 * including:
 * 1. Booking identification and type
 * 2. Booking dates and status
 * 3. Currency and amount details
 * 4. Reference and source information
 * 5. Associated books and their details
 * 
 * The class is designed to be embedded within a Deal entity
 * and provides a comprehensive set of booking-related attributes.
 * 
 * @see FixedIncomeDerivativeDeal
 * @see Book
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class BookingInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Column(name = "booking_id")
    private String bookingId;
    
    @Column(name = "booking_type")
    private String bookingType;
    
    @Column(name = "booking_date")
    private String bookingDate;
    
    @Column(name = "booking_status")
    private String bookingStatus;
    
    @Column(name = "booking_currency")
    private String bookingCurrency;
    
    @Column(name = "booking_amount")
    private String bookingAmount;
    
    @Column(name = "booking_description")
    private String bookingDescription;
    
    @Column(name = "booking_reference")
    private String bookingReference;
    
    @Column(name = "booking_source")
    private String bookingSource;
    
    @Column(name = "booking_destination")
    private String bookingDestination;
    
    @Column(name = "booking_notes")
    private String bookingNotes;
    
    private List<Book> books;
    
    /**
     * Inner class representing a book in the booking system.
     * A book is a logical grouping of deals for accounting and reporting purposes.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Book implements Serializable {
        private static final long serialVersionUID = 1L;
        
        /**
         * Unique identifier for the book
         */
        private String id;
        
        /**
         * Name of the book
         */
        private String name;
        
        /**
         * Type of the book (e.g., TRADING, HEDGING)
         */
        private String type;
        
        /**
         * Description of the book's purpose
         */
        private String description;
    }
} 