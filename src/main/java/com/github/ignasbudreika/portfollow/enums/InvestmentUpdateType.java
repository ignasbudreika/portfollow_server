package com.github.ignasbudreika.portfollow.enums;

public enum InvestmentUpdateType {
    MANUAL("Manual"),
    SPECTROCOIN("SpectroCoin account"),
    ETHEREUM_WALLET("Ethereum wallet"),
    DAILY("Periodic (daily)"),
    WEEKLY("Periodic (weekly)"),
    MONTHLY("Periodic (monthly)"),
    QUARTERLY("Periodic (quarterly)"),
    YEARLY("Periodic (yearly)");

    private String title;

    InvestmentUpdateType(String title) {
        this.title = title;
    }

    public static InvestmentUpdateType getUpdateType(PeriodicInvestmentPeriod period) {
        if (period == null) {
            return MANUAL;
        } else if (period == PeriodicInvestmentPeriod.DAILY) {
            return DAILY;
        } else if (period == PeriodicInvestmentPeriod.WEEKLY) {
            return WEEKLY;
        } else if (period == PeriodicInvestmentPeriod.MONTHLY) {
            return MONTHLY;
        } else if (period == PeriodicInvestmentPeriod.QUARTERLY) {
            return QUARTERLY;
        } else {
            return YEARLY;
        }
    }

    @Override
    public String toString() {
        return this.title;
    }
}
