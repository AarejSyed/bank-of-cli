package com.bankofcli.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.bankofcli.domain.Account;
import com.bankofcli.domain.BankTransaction;
import com.bankofcli.domain.BankTransactionType;

public class DaoImpl implements Dao {
    // Account SQL query Strings
    private static final String INSERT_ACCOUNT_SQL = "INSERT INTO account (pin) VALUES (?) RETURNING id, pin, balance";
    private static final String SELECT_ACCOUNT_BY_ID_SQL = "SELECT id, pin, balance FROM account WHERE id = ?";
    private static final String UPDATE_ACCOUNT_SQL = "UPDATE account SET pin = ?, balance = ? WHERE id = ?";

    // Bank_Transaction SQL query Strings
    private static final String INSERT_BANK_TRANSACTION_SQL = "INSERT INTO bank_transaction (account_id, amount, type) VALUES (?, ?, ?::bank_transaction_type_enum) RETURNING id, account_id, amount, timestamp, type";
    private static final String SELECT_ALL_BANK_TRANSACTIONS_BY_ACCOUNT_ID_DESCENDING_SQL = "SELECT id, account_id, amount, timestamp, type FROM bank_transaction WHERE account_id = ? ORDER BY timestamp DESC";

    public DaoImpl() {}

    // ACCOUNT METHODS

    // Insert new Account
    @Override
    public Account insertAccount(Account account) {
        // Create connection
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection()) {
            // Set auto-commit to false
            connection.setAutoCommit(false);
            
            // Run SQL prepared statement to insert Account
            try (PreparedStatement statement = connection.prepareStatement(INSERT_ACCOUNT_SQL)) {
                // Set parameters of prepared statement
                statement.setString(1, account.getPin());

                // Execute statement and capture result set
                try (ResultSet resultSet = statement.executeQuery()) {
                    // Update account with database-generated fields
                    if (resultSet.next()) {
                        Account updatedAccount = mapAccount(resultSet);
                        account.setId(updatedAccount.getId());
                        account.setBalance(updatedAccount.getBalance());
                    }

                    // If no record returned, nothing was inserted: throw exception
                    else { throw new SQLException(); }
                }

                // Commit insertion and return updated Account object
                connection.commit();
                return account;
            }

            // If Account insertion fails, roll back and throw error
            catch (SQLException e) {
                connection.rollback();
                throw databaseError("Could not insert new Account into database", e);
            }
        }

        // Catch connection failure
        catch (SQLException e) { throw databaseConnectionError(e); }
    }

    // Select Account by ID
    @Override
    public Optional<Account> selectAccountById(long id) {
        // Create connection
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection()) {
            // Run SQL prepared statement to select Account by ID
            try (PreparedStatement statement = connection.prepareStatement(SELECT_ACCOUNT_BY_ID_SQL)) {
                // Set parameters of prepared statement
                statement.setLong(1, id);

                // Execute statement and capture result set
                try (ResultSet resultSet = statement.executeQuery()) {
                    // If result exists, map to Account object and return
                    if (resultSet.next()) {
                        return Optional.of(mapAccount(resultSet));
                    }

                    // If result does not exist, return null
                    else { return Optional.empty(); }
                }
            }

            // If Account selection fails, throw error
            catch (SQLException e) {
                throw databaseError("Could not find Account with ID: " + id, e);
            }
        }

        // Catch connection failure
        catch (SQLException e) { throw databaseConnectionError(e); }
    }

    // Update existing Account
    // NOT ITS OWN TRANSACTION
    // Helper for complex transactions involving both Account and Bank_Transaction
    private void updateAccount(Connection connection, Account account) {
        // Run SQL prepared statement to update Account
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_ACCOUNT_SQL)) {
            // Set parameters of prepared statement
            statement.setString(1, account.getPin());
            statement.setBigDecimal(2, account.getBalance());
            statement.setLong(3, account.getId());

            // Execute statement and capture rows modified
            int rowsModified = statement.executeUpdate();

            // If no rows were modified, nothing was updated: throw exception
            if (rowsModified == 0) { throw new SQLException(); }
        }

        // If Account update fails, throw error
        catch (SQLException e) {
            throw databaseError("Could not update account: " + account.getId(), e);
        }
    }

    // Map SQL record --> Account object
    private Account mapAccount(ResultSet resultSet) throws SQLException {
        return new Account(
            resultSet.getLong("id"),
            resultSet.getString("pin"),
            resultSet.getBigDecimal("balance")
        );
    }

    // BANK_TRANSACTION METHODS

    @Override
    public List<BankTransaction> selectAllBankTransactionsByAccountIdDescending(long accountId) {
        List<BankTransaction> bankTransactions = new ArrayList<>();
        
        // Attempt to perform query
        try (
            // Prepare resources
            Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
            PreparedStatement statement = connection.prepareStatement(SELECT_ALL_BANK_TRANSACTIONS_BY_ACCOUNT_ID_DESCENDING_SQL);
        ) {
            // Set parameter of prepared statement
            statement.setLong(1, accountId);

            // Attempt to execute prepared statement and get results
            try (ResultSet resultSet = statement.executeQuery()) {
                // Convert results to Java objects
                while (resultSet.next()) {
                    bankTransactions.add(mapBankTransaction(resultSet));
                }

                return bankTransactions;
            }
        }
        catch (SQLException e) {
            throw databaseError("Could not find bank transactions involving account " + accountId, e);
        }
    }

    // Insert new BankTransaction
    // NOT ITS OWN TRANSACTION
    // Helper for complex transactions involving both Account and Bank_Transaction
    private BankTransaction insertBankTransaction(Connection connection, BankTransaction bankTransaction) {
        // Run SQL prepared statement to insert Bank_Transaction
        try (PreparedStatement statement = connection.prepareStatement(INSERT_BANK_TRANSACTION_SQL)) {
            // Convert `type` field in BankTransaction from Java enum to SQL enum String
            String typeString = switch (bankTransaction.getType()) {
                case DEPOSIT        -> "deposit";
                case WITHDRAWAL     -> "withdrawal";
                case TRANSFER_IN    -> "transfer_in";
                case TRANSFER_OUT   -> "transfer_out";
            };
            
            // Set parameters of prepared statement
            statement.setLong(1, bankTransaction.getAccountId());
            statement.setBigDecimal(2, bankTransaction.getAmount());
            statement.setString(3, typeString);

            // Execute statement and capture result set
            try (ResultSet resultSet = statement.executeQuery()) {
                // Update BankTransaction object with database-generated fields
                if (resultSet.next()) {
                    BankTransaction updatedBankTransaction = mapBankTransaction(resultSet);
                    bankTransaction.setId(updatedBankTransaction.getId());
                    bankTransaction.setTimestamp(updatedBankTransaction.getTimestamp());
                }

                // If no record returned, nothing was inserted: throw exception
                else { throw new SQLException(); }
            }
        }

        // If Bank_Transaction insertion fails, throw error
        catch (SQLException e) {
            throw databaseError("Could not insert new Bank_Transaction into database", e);
        }

        // Return updated BankTransaction object
        return bankTransaction;
    }

    // Map SQL record --> BankTransaction Object
    private BankTransaction mapBankTransaction(ResultSet resultSet) throws SQLException {
        // Map `type` field to Java enum
        BankTransactionType type = switch (resultSet.getString("type")) {
            case "deposit"      -> BankTransactionType.DEPOSIT;
            case "withdrawal"   -> BankTransactionType.WITHDRAWAL;
            case "transfer_in"  -> BankTransactionType.TRANSFER_IN;
            case "transfer_out" -> BankTransactionType.TRANSFER_OUT;
            default             -> throw new IllegalStateException(
                                    "Invalid bank transaction type: " +
                                    resultSet.getString("type")
                                );
        };
        
        return new BankTransaction(
            resultSet.getLong("id"),
            resultSet.getLong("account_id"),
            resultSet.getBigDecimal("amount"),
            resultSet.getObject("timestamp", OffsetDateTime.class),
            type
        );
    }

    // ACCOUNT AND BANK_TRANSACTION METHODS

    // Update existing Account and insert new standard Bank_Transaction
    @Override
    public BankTransaction updateAccountAndInsertStandardBankTransaction(Account account, BankTransaction bankTransaction) {
        // Create connection
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection()) {
            // Set auto-commit to false
            connection.setAutoCommit(false);
            
            try {
                // Update existing Account and insert new standard Bank_Transaction
                this.updateAccount(connection, account);
                this.insertBankTransaction(connection, bankTransaction);

                // Commit changes and return updated BankTransaction object
                connection.commit();
                return bankTransaction;
            }

            // If helper action fails, roll back and re-throw error
            catch (RuntimeException e) {
                try {
                    connection.rollback();
                }
                catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }

                throw e;
            }
        }

        // Catch connection failure
        catch (SQLException e) { throw databaseConnectionError(e); }
    }

    // Update existing Accounts and insert new transfer Bank_Transactions
    @Override
    public BankTransaction[] updateAccountsAndInsertTransferBankTransactions(
        Account sourceAccount,
        Account destinationAccount,
        BankTransaction sourceBankTransaction,
        BankTransaction destinationBankTransaction
    ) {
        // Create connection
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection()) {
            // Set auto-commit to false
            connection.setAutoCommit(false);
            
            try {
                // Update existing Accounts and insert new transfer Bank_Transactions
                this.updateAccount(connection, sourceAccount);
                this.updateAccount(connection, destinationAccount);
                this.insertBankTransaction(connection, sourceBankTransaction);
                this.insertBankTransaction(connection, destinationBankTransaction);

                // Commit changes and return updated BankTransaction objects
                connection.commit();
                return new BankTransaction[] {sourceBankTransaction, destinationBankTransaction};
            }

            // If helper actions fail, roll back and re-throw error
            catch (RuntimeException e) {
                try {
                    connection.rollback();
                }
                catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }

                throw e;
            }
        }

        // Catch connection failure
        catch (SQLException e) { throw databaseConnectionError(e); }
    }

    // General methods

    private IllegalStateException databaseError(String message, SQLException cause) {
        return new IllegalStateException(message, cause);
    }

    private IllegalStateException databaseConnectionError(SQLException cause) {
        return databaseError("Could not connect to database", cause);
    }
}
