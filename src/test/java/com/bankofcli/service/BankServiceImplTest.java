package com.bankofcli.service;

import com.bankofcli.domain.Account;
import com.bankofcli.persistence.BankDao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

// Unit tests for BankServiceImpl
class BankServiceImplTest {
    private BankDao bankDao;
    private BankServiceImpl bankService;

    // Set up mock BankDao and BankServiceImpl before each test
    @BeforeEach
    void setUp() {
        bankDao = mock(BankDao.class);
        bankService = new BankServiceImpl(bankDao);
    }

    // Test for successful withdrawal with sufficient funds
    @Test
    void withdrawWithSufficientFundsDecreasesBalance() {
        // Create an account with an initial balance of 51.00
        Account account = new Account(
            1L,
            "1111",
            new BigDecimal("51.00")
        );

        // Arrange for mock BankDao to return the account when queried by ID
        when(bankDao.selectAccountById(1L))
            .thenReturn(Optional.of(account));

        // Log in to the account and withdraw 40.00
        bankService.logInToAccount(1L, "1111");
        bankService.withdraw(new BigDecimal("40.00"));

        // Assert that the balance is decreased by the withdrawal amount
        assertEquals(
            new BigDecimal("11.00"),
            account.getBalance()
        );
    }

    // Test for withdrawal with insufficient funds, expecting an exception
    @Test
    void withdrawWithInsufficientFundsThrowsException() {
        // Create an account with an initial balance of 51.00
        Account account = new Account(
            1L,
            "1111",
            new BigDecimal("51.00")
        );

        // Arrange for mock BankDao to return the account when queried by ID
        when(bankDao.selectAccountById(1L))
            .thenReturn(Optional.of(account));

        // Log in to the account
        bankService.logInToAccount(1L, "1111");

        // Attempt to withdraw 57.00, which exceeds the account balance, and assert that an IllegalArgumentException is thrown
        assertThrows(
            IllegalArgumentException.class,
            () -> bankService.withdraw(new BigDecimal("57.00"))
        );

        // Assert that the balance remains unchanged after the failed withdrawal attempt
        assertEquals(
            new BigDecimal("51.00"),
            account.getBalance()
        );
    }
}
