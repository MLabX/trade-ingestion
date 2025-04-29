package com.magiccode.tradeingestion.service.validation;

import com.magiccode.tradeingestion.model.Deal;
import org.springframework.stereotype.Service;

@Service
public class DefaultDealValidationService implements DealValidationService {
    @Override
    public void validateDeal(Deal deal) {
        if (deal == null) {
            throw new IllegalArgumentException("Deal cannot be null");
        }
        if (deal.getDealId() == null || deal.getDealId().trim().isEmpty()) {
            throw new IllegalArgumentException("Deal ID cannot be null or empty");
        }
        if (deal.getClientId() == null || deal.getClientId().trim().isEmpty()) {
            throw new IllegalArgumentException("Client ID cannot be null or empty");
        }
        if (deal.getInstrumentId() == null || deal.getInstrumentId().trim().isEmpty()) {
            throw new IllegalArgumentException("Instrument ID cannot be null or empty");
        }
        if (deal.getQuantity() == null || deal.getQuantity().signum() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (deal.getPrice() == null || deal.getPrice().signum() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (deal.getCurrency() == null || !deal.getCurrency().matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("Currency must be a valid 3-letter code");
        }
        if (deal.getStatus() == null || !deal.getStatus().matches("^(NEW|PROCESSING|COMPLETED|FAILED|CONFIRMED|AMENDED|CANCELLED)$")) {
            throw new IllegalArgumentException("Invalid deal status");
        }
        if (deal.getDealDate() == null) {
            throw new IllegalArgumentException("Deal date cannot be null");
        }
    }
} 
