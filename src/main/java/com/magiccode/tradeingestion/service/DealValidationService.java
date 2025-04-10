package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.model.Deal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealValidationService {

    public List<String> validateDeal(Deal deal) {
        List<String> errors = new ArrayList<>();

        // Validate quantity
        if (deal.getQuantity() == null || deal.getQuantity().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            errors.add("Quantity must be positive");
        }

        // Validate price
        if (deal.getPrice() == null || deal.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            errors.add("Price must be positive");
        }

        // Validate counterparty
        if (deal.getClientId() == null || deal.getClientId().trim().isEmpty()) {
            errors.add("Client ID is required");
        } else if (!isValidCounterparty(deal.getClientId())) {
            errors.add("Invalid client ID: " + deal.getClientId());
        }

        // Validate instrument
        if (deal.getInstrumentId() == null || deal.getInstrumentId().trim().isEmpty()) {
            errors.add("Instrument ID is required");
        } else if (!isValidInstrument(deal.getInstrumentId())) {
            errors.add("Invalid instrument ID: " + deal.getInstrumentId());
        }

        return errors;
    }

    @Cacheable(value = "counterparties", key = "#counterpartyId")
    public boolean isValidCounterparty(String counterpartyId) {
        // In a real application, this would check against a reference data service
        log.debug("Validating counterparty: {}", counterpartyId);
        return counterpartyId != null && counterpartyId.length() >= 3;
    }

    @Cacheable(value = "instruments", key = "#instrumentId")
    public boolean isValidInstrument(String instrumentId) {
        // In a real application, this would check against a reference data service
        log.debug("Validating instrument: {}", instrumentId);
        return instrumentId != null && instrumentId.length() >= 2;
    }
}