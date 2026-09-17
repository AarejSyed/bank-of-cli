package com.bankofcli.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.math.BigDecimal;

import com.bankofcli.domain.Account;

public class AccountDaoImpl implements AccountDao {
    private static final String INSERT_SQL = "INSERT INTO account (username, pin) VALUES (?, ?)";
    private static final String FIND_BY_USERNAME_SQL = "SELECT * FROM account WHERE username = ?";
    private static final String UPDATE_BALANCE_SQL = "UPDATE account SET balance = ? WHERE id = ?";

    public AccountDaoImpl() {}

    @Override
    public void registerAccount(String username, String pin) {
        try (
            Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
            PreparedStatement statement = connection.prepareStatement(INSERT_SQL)
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
            PreparedStatement statement = connection.prepareStatement(FIND_BY_USERNAME_SQL);
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
    public void updateAccountBalance(long accountId, BigDecimal newBalance) {
        try (
            Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
            PreparedStatement statement = connection.prepareStatement(UPDATE_BALANCE_SQL)
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

    private IllegalStateException databaseError(String message, SQLException cause) {
        return new IllegalStateException(message, cause);
    }
}
