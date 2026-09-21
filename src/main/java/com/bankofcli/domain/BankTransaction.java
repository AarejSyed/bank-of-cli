package com.bankofcli.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class BankTransaction {
    private long id;
    private long accountId;
    private BigDecimal amount;
    private OffsetDateTime timestamp;
    private BankTransactionType type;

    // New BankTransaction to be recorded
    public BankTransaction(
        long accountId,
        BigDecimal amount,
        BankTransactionType type
    ) {
        this.accountId = accountId;
        this.amount = amount;
        this.type = type;
    }

    // Existing BankTransaction that is already recorded
    public BankTransaction(
        long id,
        long accountId,
        BigDecimal amount,
        OffsetDateTime timestamp,
        BankTransactionType type
    ) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.timestamp = timestamp;
        this.type = type;
    }

    public long getId() { return id; }
    public long getAccountId() { return accountId; }
    public BigDecimal getAmount() { return amount; }
    public OffsetDateTime getTimestamp() { return timestamp; }
    public BankTransactionType getType() { return type; }

    public void setId(long id) { this.id = id; }
    public void setAccountId(long accountId) { this.accountId = accountId; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }
    public void setType(BankTransactionType type) { this.type = type; }
}
