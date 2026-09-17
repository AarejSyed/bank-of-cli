package com.bankofcli.persistence;

import java.math.BigDecimal;
import java.util.List;

import com.bankofcli.domain.Account;
import com.bankofcli.domain.BankTransaction;
import com.bankofcli.domain.BankTransactionType;

// Data Access Object for Account and Bank_Transaction
public interface Dao {
    // Account
    void addAccount(String username, String pin);
    Account getAccountByUsername(String username);
    void setAccountBalance(long accountId, BigDecimal newBalance);

    // Bank_Transaction
    void addBankTransaction(
        long accountId,
        BigDecimal amount,
        BankTransactionType type
    );
    List<BankTransaction> getAllBankTransactionsByAccountIdDescending(long accountId);
}
