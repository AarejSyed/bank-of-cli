package com.bankofcli.persistence;

import java.util.List;
import java.util.Optional;

import com.bankofcli.domain.Account;
import com.bankofcli.domain.BankTransaction;

// Data Access Object interface for Account and BankTransaction
public interface Dao {
    // Account
    Account insertAccount(Account account);
    Optional<Account> selectAccountById(long accountId);

    // BankTransaction
    List<BankTransaction> selectAllBankTransactionsByAccountIdDescending(long accountId);

    // Account and BankTransaction
    BankTransaction updateAccountAndInsertStandardBankTransaction(
        Account account,
        BankTransaction bankTransaction
    );
    BankTransaction[] updateAccountsAndInsertTransferBankTransactions(
        Account sourceAccount,
        Account destinationAccount,
        BankTransaction sourceBankTransaction,
        BankTransaction destinationBankTransaction
    );
}
