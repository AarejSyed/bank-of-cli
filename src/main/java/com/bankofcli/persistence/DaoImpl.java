package com.bankofcli.persistence;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.bankofcli.domain.Account;
import com.bankofcli.domain.BankTransaction;
import com.bankofcli.domain.BankTransactionType;

public class DaoImpl implements Dao {
    // Account SQL query Strings
    private static final String INSERT_ACCOUNT_SQL = "INSERT INTO account (username, pin) VALUES (?, ?)";
    private static final String FIND_ACCOUNT_BY_USERNAME_SQL = "SELECT * FROM account WHERE username = ?";
    private static final String UPDATE_ACCOUNT_BALANCE_SQL = "UPDATE account SET balance = ? WHERE id = ?";

    // Bank_Transaction SQL query Strings
    private static final String INSERT_BANK_TRANSACTION_SQL = "INSERT INTO bank_transaction (account_id, amount, type) VALUES (?, ?, ?::bank_transaction_type_enum)";
    private static final String FIND_ALL_BANK_TRANSACTIONS_BY_ACCOUNT_ID_DESCENDING_SQL = "SELECT * FROM bank_transaction WHERE account_id = ? ORDER BY timestamp DESC";

    public DaoImpl() {}

    // Account methods

    @Override
    public void addAccount(String username, String pin) {
        try (
            Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
            PreparedStatement statement = connection.prepareStatement(INSERT_ACCOUNT_SQL)
        ) {
            statement.setString(1, username);
            statement.setString(2, pin);

            statement.executeUpdate();
        }
        catch (SQLException e) {
            throw databaseError("Could not register account", e);
        }
    }

    @Override
    public Account getAccountByUsername(String username) {
        try (
            Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
            PreparedStatement statement = connection.prepareStatement(FIND_ACCOUNT_BY_USERNAME_SQL);
        ) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapAccount(resultSet);
                }
            }
            
            return null;
        }
        catch (SQLException e) {
            throw databaseError("Could not find account with username " + username, e);
        }
    }

    @Override
    public void setAccountBalance(long accountId, BigDecimal newBalance) {
        try (
            Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
            PreparedStatement statement = connection.prepareStatement(UPDATE_ACCOUNT_BALANCE_SQL)
        ) {
            statement.setBigDecimal(1, newBalance);
            statement.setLong(2, accountId);

            statement.executeUpdate();
        }
        catch (SQLException e) {
            throw databaseError("Could not update balance of account " + accountId + " to $" + newBalance, e);
        }
    }

    private Account mapAccount(ResultSet resultSet) throws SQLException {
        return new Account(
            resultSet.getLong("id"),
            resultSet.getString("username"),
            resultSet.getString("pin"),
            resultSet.getBigDecimal("balance")
        );
    }

    // Bank_Transaction methods

    @Override
    public void addBankTransaction(
        long accountId,
        BigDecimal amount,
        BankTransactionType type
    ) {
        // Attempt to perform query
        try (
            // Prepare resources
            Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
            PreparedStatement statement = connection.prepareStatement(INSERT_BANK_TRANSACTION_SQL)
        ) {
            // Convert `type` from Java enum to SQL enum String
            String typeString = switch (type) {
                case DEPOSIT        -> "deposit";
                case WITHDRAWAL     -> "withdrawal";
                case TRANSFER_IN    -> "transfer_in";
                case TRANSFER_OUT   -> "transfer_out";
            };

            // Set parameters of prepared statement
            statement.setLong(1, accountId);
            statement.setBigDecimal(2, amount);
            statement.setString(3, typeString);

            statement.executeUpdate();
        }
        catch (SQLException e) {
            throw databaseError("Could not perform transaction", e);
        }
    }

    @Override
    public List<BankTransaction> getAllBankTransactionsByAccountIdDescending(long accountId) {
        List<BankTransaction> bankTransactions = new ArrayList<>();
        
        // Attempt to perform query
        try (
            // Prepare resources
            Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
            PreparedStatement statement = connection.prepareStatement(FIND_ALL_BANK_TRANSACTIONS_BY_ACCOUNT_ID_DESCENDING_SQL);
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
            throw databaseError("Could not list bank transactions involving this account", e);
        }
    }

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

    // General methods

    private IllegalStateException databaseError(String message, SQLException cause) {
        return new IllegalStateException(message, cause);
    }
}
