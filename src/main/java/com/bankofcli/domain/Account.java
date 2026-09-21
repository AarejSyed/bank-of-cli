package com.bankofcli.domain;

import java.math.BigDecimal;

public class Account {
    private long id;
    private String pin;
    private BigDecimal balance;

    public Account(long id, String username, String pin, BigDecimal balance) {
        this.id = id;
        this.pin = pin;
        this.balance = balance;
    }

    public long getId() { return id; }
    public String getPin() { return pin; }
    public BigDecimal getBalance() { return balance; }
}
