package com.magiccode.tradeingestion.service;

import com.magiccode.tradeingestion.exception.DealProcessingException;
import com.magiccode.tradeingestion.model.Deal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealValidationService {

    public List<String> validateDeal(Deal deal) {
        List<String> errors = new ArrayList<>();
        
        // Validate notional amount
        if (deal.getNotionalAmount() != null && 
            deal.getNotionalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Notional amount must be greater than zero");
        }
        
        // Validate instrument quantity
        if (deal.getInstrumentQuantity() != null && 
            deal.getInstrumentQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Instrument quantity must be greater than zero");
        }
        
        // Validate instrument price
        if (deal.getInstrumentPrice() != null && 
            deal.getInstrumentPrice().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Instrument price cannot be negative");
        }
        
        // Validate settlement amount
        if (deal.getInstrumentSettlementAmount() != null && 
            deal.getInstrumentSettlementAmount().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Settlement amount cannot be negative");
        }
        
        return errors;
    }
    
    public void validateDealOrThrow(Deal deal) {
        List<String> errors = validateDeal(deal);
        if (!errors.isEmpty()) {
            throw new DealProcessingException("Deal validation failed: " + String.join(", ", errors));
        }
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