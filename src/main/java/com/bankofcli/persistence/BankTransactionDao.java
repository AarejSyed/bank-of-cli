package com.bankofcli.persistence;

import com.bankofcli.domain.BankTransaction;
import com.bankofcli.domain.BankTransactionType;

import java.math.BigDecimal;
import java.util.List;

public interface BankTransactionDao {
    void registerBankTransaction(
        long accountId,
        BigDecimal amount,
        BankTransactionType type
    );

    List<BankTransaction> getAllBankTransactionsByAccountIdDescending(long accountId);
}
