package dataaccess;

import model.UserData;

import java.sql.*;

public class SQLUserDAO implements UserDAO {

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var pStatement = conn.prepareStatement(statement)) {
                    pStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format
                    ("Unable to configure database: %s", e.getMessage()));
        }
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  users (
              `username` varchar(256) NOT NULL,
              `password` varchar(256) NOT NULL,
              `email` varchar(256) NOT NULL,
              PRIMARY KEY (`username`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    @Override
    public UserData createUser(UserData user) throws DataAccessException {
        configureDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
            try (var pStatement = conn.prepareStatement(statement)) {
                pStatement.setString(1, user.username());
                pStatement.setString(2, user.password());
                pStatement.setString(3, user.email());
                pStatement.executeUpdate();
                return new UserData(user.username(), user.password(), user.email());
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format
                    ("Unable to read data: %s", e.getMessage()));
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        configureDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM users WHERE username=?";
            try (var pStatement = conn.prepareStatement(statement)) {
                pStatement.setString(1, username);
                try (var resultSet = pStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return new UserData(resultSet.getString("username"),
                                resultSet.getString("password"),
                                resultSet.getString("email"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format
                    ("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    @Override
    public void clear() throws DataAccessException {
        configureDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "TRUNCATE users";
            try (var pStatement = conn.prepareStatement(statement)) {
                pStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}