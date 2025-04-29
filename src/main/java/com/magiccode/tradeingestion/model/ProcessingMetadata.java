package com.magiccode.tradeingestion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents processing metadata for a financial deal.
 * 
 * This class encapsulates all processing-related attributes to track
 * the lifecycle and status of deal processing, including:
 * 1. Current processing status
 * 2. Processing start and end times
 * 3. Processing errors and retry information
 * 
 * The class is designed to be embedded within a Deal entity
 * and provides a comprehensive view of the deal's processing
 * lifecycle and any associated issues.
 * 
 * @see Deal
 * @see FixedIncomeDerivativeDeal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ProcessingMetadata {
    /**
     * Current status of the processing (e.g., PENDING, IN_PROGRESS, COMPLETED, FAILED)
     */
    @Column(name = "processing_status")
    private String processingStatus;
    
    /**
     * Timestamp when processing of the deal started
     */
    @Column(name = "processing_start_time")
    private LocalDateTime processingStartTime;
    
    /**
     * Timestamp when processing of the deal ended
     */
    @Column(name = "processing_end_time")
    private LocalDateTime processingEndTime;
    
    /**
     * Any errors encountered during processing
     */
    @Column(name = "processing_errors")
    private String processingErrors;
    
    /**
     * Number of times the processing has been retried
     */
    @Column(name = "processing_retry_count")
    private Integer retryCount;
} 