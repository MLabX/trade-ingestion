package com.magiccode.tradeingestion.service.transformation;

import com.magiccode.tradeingestion.model.Deal;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultDealTransformationService<T extends Deal> implements DealTransformationService<T> {

    @Override
    @Timed(value = "deal.transformation.time", description = "Time taken to transform a deal")
    public T transform(T deal) {
        log.info("Transforming deal: {}", deal.getDealId());
        
        // In a real application, this would contain complex transformation logic
        // For now, we'll just return the deal as is
        
        log.info("Deal transformation completed: {}", deal.getDealId());
        
        return deal;
    }
} 