package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class responsible for managing trade deal ingestion and retrieval operations.
 * 
 * This service provides core functionality for:
 * 1. Processing and persisting new deals
 * 2. Retrieving deals by various criteria
 * 3. Managing deal lifecycle
 * 
 * The service is transactional, ensuring data consistency across operations.
 * It leverages the DealRepository for persistence operations and provides
 * a clean API for deal management.
 * 
 * Key features:
 * - Transactional operations
 * - Deal processing and validation
 * - Flexible deal retrieval options
 * - Integration with repository layer
 * 
 * @see Deal
 * @see DealRepository
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TradeIngestionService {

    /**
     * Repository for deal persistence operations
     */
    private final DealRepository dealRepository;

    /**
     * Processes and persists a new deal.
     * 
     * @param deal The deal to be processed and persisted
     * @return The processed deal with any updates from persistence
     */
    public Deal processDeal(Deal deal) {
        return dealRepository.save(deal);
    }

    /**
     * Retrieves a deal by its unique identifier.
     * 
     * @param id The UUID of the deal to retrieve
     * @return An Optional containing the deal if found, empty otherwise
     */
    public Optional<Deal> getDealById(UUID id) {
        return dealRepository.findById(id);
    }

    /**
     * Retrieves all deals in the system.
     * 
     * @return A list of all deals
     */
    public List<Deal> getAllDeals() {
        return dealRepository.findAll();
    }

    /**
     * Retrieves deals associated with a specific instrument.
     * 
     * @param instrumentId The identifier of the instrument
     * @return A list of deals for the specified instrument
     */
    public List<Deal> getDealsByInstrumentId(String instrumentId) {
        return dealRepository.findByInstrumentId(instrumentId);
    }

    /**
     * Retrieves deals by their symbol.
     * 
     * @param symbol The symbol to search for
     * @return A list of deals matching the symbol
     */
    public List<Deal> getDealsBySymbol(String symbol) {
        return dealRepository.findByInstrumentId(symbol);
    }
} 