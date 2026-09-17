package com.bankofcli.persistence;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;

import com.bankofcli.domain.BankTransaction;
import com.bankofcli.domain.BankTransactionType;

public class BankTransactionDaoImpl implements BankTransactionDao {
    private static final String INSERT_SQL = "INSERT INTO bank_transaction (account_id, amount, type) VALUES (?, ?, ?::bank_transaction_type_enum)";
    private static final String FIND_ALL_BY_ACCOUNT_ID_DESCENDING_SQL = "SELECT * FROM bank_transaction WHERE account_id = ? ORDER BY timestamp DESC";

    public BankTransactionDaoImpl() {}

    @Override
    public void registerBankTransaction(
        long accountId,
        BigDecimal amount,
        BankTransactionType type
    ) {
        // Attempt to perform query
        try (
            // Prepare resources
            Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
            PreparedStatement statement = connection.prepareStatement(INSERT_SQL)
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
            PreparedStatement statement = connection.prepareStatement(FIND_ALL_BY_ACCOUNT_ID_DESCENDING_SQL);
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

    private IllegalStateException databaseError(String message, SQLException cause) {
        return new IllegalStateException(message, cause);
    }
}
