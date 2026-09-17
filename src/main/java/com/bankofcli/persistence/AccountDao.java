package com.bankofcli.persistence;

import java.math.BigDecimal;

import com.bankofcli.domain.Account;

public interface AccountDao {
    void registerAccount(String username, String pin);

    Account getAccountByUsername(String username);

    void updateAccountBalance(long accountId, BigDecimal newBalance);
}
