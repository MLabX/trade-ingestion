package com.magiccode.tradeingestion.repository;

import com.magiccode.tradeingestion.model.FixedIncomeDerivativeDeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FixedIncomeDerivativeDealRepository extends JpaRepository<FixedIncomeDerivativeDeal, UUID> {
    Optional<FixedIncomeDerivativeDeal> findByDealId(String dealId);
    List<FixedIncomeDerivativeDeal> findByInstrumentId(String instrumentId);
    List<FixedIncomeDerivativeDeal> findByDealType(String dealType);
    List<FixedIncomeDerivativeDeal> findByStatus(String status);
    List<FixedIncomeDerivativeDeal> findByCounterpartyEntityId(String entityId);
} 