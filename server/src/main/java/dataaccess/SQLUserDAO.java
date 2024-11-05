package dataaccess;

import com.google.gson.Gson;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class SQLUserDAO implements UserDAO {

    private String hashPass(String pass){
        return BCrypt.hashpw(pass, BCrypt.gensalt());
    }

    private String checkPass(String hashedPass){
        return null;
    }

    public void configureDatabase() throws DataAccessException {
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
              `user` varchar(256) NOT NULL,
              `password` varchar(256) NOT NULL,
              `email` varchar(256) NOT NULL,
              PRIMARY KEY (`user`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    private int executeUpdate(String statement, Object... objects) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < objects.length; i++) {
                    var object = objects[i];
                    if (object instanceof String p) ps.setString(i + 1, p);
                    else if (object instanceof Integer p) ps.setInt(i + 1, p);
                    else if (object instanceof UserData p) ps.setString(i + 1, p.toString());
                    else if (object == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();

                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format
                    ("Unable to update database: %s, %s", statement, e.getMessage()));
        }
    }


    private UserData readUser(ResultSet resultSet) throws SQLException {
        var json = resultSet.getString("json");
        return new Gson().fromJson(json, UserData.class);
    }


    @Override
    public UserData createUser(UserData user) throws DataAccessException {
        configureDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "INSERT INTO users (user, password, email, json) VALUES (?, ?, ?, ?)";
            try (var pStatement = conn.prepareStatement(statement)) {
                pStatement.setString(1, user.username());
                pStatement.setString(2, hashPass(user.password()));
                pStatement.setString(3, user.email());
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
                        return readUser(resultSet);
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
        var statement = "TRUNCATE users";
        executeUpdate(statement);
    }


}
