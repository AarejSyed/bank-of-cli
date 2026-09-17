package com.bankofcli.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class BankTransaction {
    private long id;
    private long accountId;
    private BigDecimal amount;
    private OffsetDateTime timestamp;
    private BankTransactionType type;

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
}
