package com.magiccode.tradeingestion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents booking information for a financial deal.
 * 
 * This class encapsulates all the necessary information for booking a deal,
 * including:
 * 1. Booking identification and type
 * 2. Associated books and their details
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
@Entity
@Table(name = "booking_info")
public class BookingInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToMany(mappedBy = "bookingInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Book> books = new ArrayList<>();
    
    /**
     * Represents a book in the booking system.
     * A book is a logical grouping of deals for accounting and reporting purposes.
     */
    @Entity
    @Table(name = "book")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Book implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "booking_info_id", nullable = false)
        private BookingInfo bookingInfo;
        
        @NotNull
        @Column(name = "book_code", nullable = false)
        private String bookCode;
        
        @NotNull
        @Column(name = "book_name", nullable = false)
        private String bookName;
        
        @NotNull
        @Column(name = "book_type", nullable = false)
        private String bookType;
        
        @NotNull
        @Column(name = "book_currency", nullable = false, length = 3)
        private String bookCurrency;
    }
} 