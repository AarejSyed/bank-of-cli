package com.bankofcli.domain;

import java.math.BigDecimal;

public class Account {
    private long id;
    private String pin;
    private BigDecimal balance;

    // New account to be registered
    public Account(String pin) {
        this.pin = pin;
    }

    // Existing account that is already registered
    public Account(long id, String pin, BigDecimal balance) {
        this.id = id;
        this.pin = pin;
        this.balance = balance;
    }

    public long getId() { return id; }
    public String getPin() { return pin; }
    public BigDecimal getBalance() { return balance; }

    public void setId(long id) { this.id = id; }
    public void setPin(String pin) { this.pin = pin; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
