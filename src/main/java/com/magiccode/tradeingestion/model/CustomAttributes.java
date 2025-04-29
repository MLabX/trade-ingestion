package com.magiccode.tradeingestion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class CustomAttributes {
    @Column(name = "custom_trade_tag")
    private String customTradeTag;
    
    @Column(name = "client_strategy")
    private String clientStrategy;
    
    @Column(name = "booking_source")
    private String bookingSource;
} 