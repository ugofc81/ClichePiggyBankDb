package com.example.clichepiggybank.model;

public enum SanctionAmount {
    STANDARD (1),
    LOW (0.5),
    HIGH (2);

    private final double amount;
    SanctionAmount(double amount) {
        this.amount = amount;
    }
    public double amount() { return amount; }
}
