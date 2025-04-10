package com.magiccode.tradeingestion.service;

public interface TradeService {
    void processTrade(String symbol, double amount, int quantity, String side);
}
