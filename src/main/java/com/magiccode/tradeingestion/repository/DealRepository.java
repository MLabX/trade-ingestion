package com.magiccode.tradeingestion.repository;

import com.magiccode.tradeingestion.model.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DealRepository extends JpaRepository<Deal, UUID> {
    Optional<Deal> findByDealId(String dealId);
    List<Deal> findByInstrumentId(String instrumentId);
} 