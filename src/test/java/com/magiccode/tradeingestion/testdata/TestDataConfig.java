package com.magiccode.tradeingestion.testdata;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for test data file paths.
 * Allows customization of test data file locations through properties.
 */
@Configuration
@ConfigurationProperties(prefix = "test.data")
public class TestDataConfig {
    private String baseDealPath = "base-deal.json";
    private String fixedIncomeDerivativeDealPath = "fixed-income-derivative-deal.json";
    private String newDealPath = "test-data/deals/FIXED_INCOME_DERIVATIVE_new.json";
    private String confirmedDealPath = "test-data/deals/FIXED_INCOME_DERIVATIVE_confirmation.json";
    private String amendedDealPath = "test-data/deals/FIXED_INCOME_DERIVATIVE_amendment.json";
    private String cancelledDealPath = "test-data/deals/FIXED_INCOME_DERIVATIVE_cancellation.json";

    public String getBaseDealPath() {
        return baseDealPath;
    }

    public void setBaseDealPath(String baseDealPath) {
        this.baseDealPath = baseDealPath;
    }

    public String getFixedIncomeDerivativeDealPath() {
        return fixedIncomeDerivativeDealPath;
    }

    public void setFixedIncomeDerivativeDealPath(String fixedIncomeDerivativeDealPath) {
        this.fixedIncomeDerivativeDealPath = fixedIncomeDerivativeDealPath;
    }

    public String getNewDealPath() {
        return newDealPath;
    }

    public void setNewDealPath(String newDealPath) {
        this.newDealPath = newDealPath;
    }

    public String getConfirmedDealPath() {
        return confirmedDealPath;
    }

    public void setConfirmedDealPath(String confirmedDealPath) {
        this.confirmedDealPath = confirmedDealPath;
    }

    public String getAmendedDealPath() {
        return amendedDealPath;
    }

    public void setAmendedDealPath(String amendedDealPath) {
        this.amendedDealPath = amendedDealPath;
    }

    public String getCancelledDealPath() {
        return cancelledDealPath;
    }

    public void setCancelledDealPath(String cancelledDealPath) {
        this.cancelledDealPath = cancelledDealPath;
    }
} 