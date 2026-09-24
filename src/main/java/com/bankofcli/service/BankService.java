package com.bankofcli.service;

import java.math.BigDecimal;
import java.util.List;

import com.bankofcli.domain.BankTransaction;

// Service layer interface
public interface BankService {
    // Secure access
    long registerAccount(String pin);
    boolean logInToAccount(long accountId, String pin);
    void logOutOfAccount();
    boolean isLoggedIn();

    // Balance management
    BigDecimal getAccountBalance();

    // Transaction engine
    void deposit(BigDecimal amount);
    void withdraw(BigDecimal amount);
    void transfer(BigDecimal amount, long destinationAccountId);

    // Audit trail
    List<BankTransaction> getBankTransactionHistory();
}
