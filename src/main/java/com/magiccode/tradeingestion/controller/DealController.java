package com.magiccode.tradeingestion.controller;

import com.magiccode.tradeingestion.exception.DealProcessingException;
import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.service.DealIngestionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/deals")
public class DealController {
    private static final Logger logger = LoggerFactory.getLogger(DealController.class);

    private final DealIngestionService dealIngestionService;

    @Autowired
    public DealController(DealIngestionService dealIngestionService) {
        this.dealIngestionService = dealIngestionService;
    }

    @PostMapping
    public ResponseEntity<Deal> createDeal(@Valid @RequestBody Deal deal) {
        try {
            logger.info("Received request to create deal: {}", deal);
            Deal savedDeal = dealIngestionService.processDeal(deal);
            logger.info("Deal created successfully: {}", savedDeal);
            return new ResponseEntity<>(savedDeal, HttpStatus.CREATED);
        } catch (DealProcessingException e) {
            logger.error("Error creating deal: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Deal> getDealById(@PathVariable UUID id) {
        try {
            logger.info("Received request to get deal by ID: {}", id);
            return dealIngestionService.getDealById(id)
                .map(deal -> new ResponseEntity<>(deal, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (DealProcessingException e) {
            logger.error("Error retrieving deal: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<Deal>> getAllDeals() {
        try {
            logger.info("Received request to get all deals");
            List<Deal> deals = dealIngestionService.getAllDeals();
            return new ResponseEntity<>(deals, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving all deals: {}", e.getMessage(), e);
            throw new DealProcessingException("Failed to retrieve deals", e);
        }
    }

    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<List<Deal>> getDealsBySymbol(@PathVariable String symbol) {
        try {
            logger.info("Received request to get deals by symbol: {}", symbol);
            List<Deal> deals = dealIngestionService.getDealsBySymbol(symbol);
            return new ResponseEntity<>(deals, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving deals by symbol: {}", e.getMessage(), e);
            throw new DealProcessingException("Failed to retrieve deals by symbol", e);
        }
    }

    @ExceptionHandler(DealProcessingException.class)
    public ResponseEntity<String> handleDealProcessingException(DealProcessingException e) {
        logger.error("Deal processing error: {}", e.getMessage(), e);
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
} 