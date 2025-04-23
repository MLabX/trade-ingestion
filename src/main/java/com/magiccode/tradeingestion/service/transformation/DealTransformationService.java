package com.magiccode.tradeingestion.service.transformation;

import com.magiccode.tradeingestion.model.Deal;

public interface DealTransformationService<T extends Deal> {
    T transform(T deal);
} 