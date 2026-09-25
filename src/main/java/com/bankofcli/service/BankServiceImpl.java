package com.bankofcli.service;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bankofcli.domain.Account;
import com.bankofcli.domain.BankTransaction;
import com.bankofcli.domain.BankTransactionType;
import com.bankofcli.persistence.BankDao;

// Service layer concretion
public class BankServiceImpl implements BankService {
    private final BankDao bankDao;
    private Account loggedInAccount;
    private static Logger logger;

    // Constructor: requires Bank DAO
    public BankServiceImpl(BankDao bankDao) {
        this.bankDao = bankDao;
        loggedInAccount = null;
        logger = LoggerFactory.getLogger(BankServiceImpl.class);
    }

    // Register new account
    @Override
    public long registerAccount(String pin) throws IllegalArgumentException {
        if (!pin.matches("\\d{4}")) {
            logger.error("User attempted to register account with invalid PIN format");
            throw new IllegalArgumentException("PIN must consist of four digits");
        }
        
        Account newAccount = new Account(pin);
        bankDao.insertAccount(newAccount);
        logger.info("Registered new account with ID: {}", newAccount.getId());
        return newAccount.getId();
    }

    // Log in to account
    @Override
    public boolean logInToAccount(long accountId, String pin) throws NoSuchElementException {
        // Retrieve account, and throw exception if it doesn't exist
        Account account = bankDao.selectAccountById(accountId).orElseThrow(
            () -> {
                logger.error("User attempted to log in to non-existent account ID: {}", accountId);
                return new NoSuchElementException("Account " + accountId + " does not exist");
            }
        );

        // Successfully log in if supplied PIN matches actual account PIN
        boolean loggedIn = account.getPin().equals(pin);
        if (loggedIn) { loggedInAccount = account; }
        logger.info("Logged in account ID: {}", loggedInAccount.getId());
        
        return loggedIn;
    }

    // Log out of account
    @Override
    public void logOutOfAccount() throws RuntimeException {
        if (!isLoggedIn()) { throw new RuntimeException("Cannot log out while not logged into account"); }
        
        logger.info("Logged out account ID: {}", loggedInAccount.getId());
        loggedInAccount = null;
    }

    // Get account balance
    @Override
    public BigDecimal getAccountBalance() throws RuntimeException { 
        if (!isLoggedIn()) { throw new RuntimeException("Cannot access account balance while not logged into account"); }
        
        return loggedInAccount.getBalance();
    }

    // Deposit money into account
    @Override
    public void deposit(BigDecimal amount) throws RuntimeException, IllegalArgumentException {
        if (!isLoggedIn()) { throw new RuntimeException("Cannot deposit money while not logged into account"); }
        if (amount.signum() <= 0) { throw new IllegalArgumentException("Amount of money deposited must be positive"); }
        
        logger.info("Depositing ${} into account ID: {}", amount, loggedInAccount.getId());
        loggedInAccount.setBalance(loggedInAccount.getBalance().add(amount));
        standardBankTransaction(BankTransactionType.DEPOSIT, amount);
    }

    // Withdraw money from account
    @Override
    public void withdraw(BigDecimal amount) throws RuntimeException, IllegalArgumentException {
        if (!isLoggedIn()) { throw new RuntimeException("Cannot withdraw money while not logged into account"); }
        if (amount.signum() <= 0) { throw new IllegalArgumentException("Amount of money withdrawn must be positive"); }
        if (amount.compareTo(loggedInAccount.getBalance()) > 0) { throw new IllegalArgumentException("Amount of money withdrawn cannot exceed account balance"); }

        logger.info("Withdrawing ${} from account ID: {}", amount,loggedInAccount.getId());
        loggedInAccount.setBalance(loggedInAccount.getBalance().subtract(amount));
        standardBankTransaction(BankTransactionType.WITHDRAWAL, amount);
    }

    // HELPER METHOD: Perform standard bank transaction
    private void standardBankTransaction(BankTransactionType type, BigDecimal amount) {
        bankDao.updateAccountAndInsertStandardBankTransaction(
            loggedInAccount,
            new BankTransaction(
                loggedInAccount.getId(),
                amount,
                type
            )
        );
    }

    // Transfer money to another account
    @Override
    public void transfer(BigDecimal amount, long destinationAccountId) throws RuntimeException, NoSuchElementException, IllegalArgumentException {
        if (!isLoggedIn()) { throw new RuntimeException("Cannot transfer money while not logged into account"); }
        Account destinationAccount = bankDao.selectAccountById(destinationAccountId).orElseThrow(
            () -> new NoSuchElementException("Cannot transfer money to non-existent account " + destinationAccountId)
        );
        if (amount.signum() <= 0) { throw new IllegalArgumentException("Amount of money transferred must be positive"); }
        if (amount.compareTo(loggedInAccount.getBalance()) > 0) { throw new IllegalArgumentException("Amount of money transferred cannot exceed account balance"); }

        logger.info("Transferring ${} from account ID {} to account ID {}", amount, loggedInAccount.getId(), destinationAccount.getId());
        loggedInAccount.setBalance(loggedInAccount.getBalance().subtract(amount));
        destinationAccount.setBalance(destinationAccount.getBalance().add(amount));
        transferBankTransaction(amount, destinationAccount);
    }

    // HELPER METHOD: Perform transfer bank transaction
    private void transferBankTransaction(BigDecimal amount, Account destinationAccount) {
        bankDao.updateAccountsAndInsertTransferBankTransactions(
            loggedInAccount,
            destinationAccount,
            new BankTransaction(
                loggedInAccount.getId(),
                amount,
                BankTransactionType.TRANSFER_OUT
            ),
            new BankTransaction(
                destinationAccount.getId(),
                amount,
                BankTransactionType.TRANSFER_IN
            )
        );
    }

    // Get account's bank transaction history sorted by time (descending)
    @Override
    public List<BankTransaction> getBankTransactionHistory() throws RuntimeException {
        if (!isLoggedIn()) { throw new RuntimeException("Cannot access transaction history while not logged into account"); }

        return bankDao.selectAllBankTransactionsByAccountIdDescending(loggedInAccount.getId());
    }

    // Check if user is logged into account
    public boolean isLoggedIn() { return this.loggedInAccount != null; }

    // Get account ID of logged in account
    public long getAccountId() throws RuntimeException {
        if (!isLoggedIn()) { throw new RuntimeException("Cannot access account ID while not logged into account"); }

        return this.loggedInAccount.getId();
    }

    // Check if account exists
    public boolean accountExists(long accountId) {
        return bankDao.selectAccountById(accountId).isPresent();
    }
}
