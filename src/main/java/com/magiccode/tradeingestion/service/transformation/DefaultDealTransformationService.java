package com.magiccode.tradeingestion.service.transformation;

import com.magiccode.tradeingestion.model.Deal;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultDealTransformationService<T extends Deal> implements DealTransformationService<T> {

    private final ConcurrentHashMap<String, Boolean> transformedDeals = new ConcurrentHashMap<>();

    @Override
    @Timed(value = "deal.transformation.time", description = "Time taken to transform a deal")
    public T transform(T deal) {
        String dealId = deal.getDealId();
        
        // Check if deal was already transformed
        if (transformedDeals.putIfAbsent(dealId, true) != null) {
            log.debug("Deal {} was already transformed, skipping", dealId);
            return deal;
        }
        
        log.info("Transforming deal: {}", dealId);
        
        // In a real application, this would contain complex transformation logic
        // For now, we'll just return the deal as is
        
        log.info("Deal transformation completed: {}", dealId);
        
        return deal;
    }
} 