package dataaccess;

import exceptions.DataAccessException;
import model.AuthData;

import java.sql.SQLException;
import java.util.ArrayList;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class SQLAuthDAO implements AuthDAO {

    private void configureDB() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var s : createStatements) {
                try (var pStatement = conn.prepareStatement(s)) {
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
            CREATE TABLE IF NOT EXISTS  auths (
              `user` varchar(256) NOT NULL,
              `authToken` varchar(256) NOT NULL,
              PRIMARY KEY (`authToken`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    private void executeUpdate(String statement, Object... objects) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < objects.length; i++) {
                    var object = objects[i];
                    if (object instanceof String p){ ps.setString(i + 1, p);}
                    else if (object instanceof Integer p) {ps.setInt(i + 1, p);}
                    else if (object instanceof AuthData p) {ps.setString(i + 1, p.toString());}
                    else if (object == null) {ps.setNull(i + 1, NULL);}
                }
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format
                    ("Unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    //used for testing
    @Override
    public int authsSize() throws DataAccessException {
        configureDB();
        ArrayList<AuthData> auths = new ArrayList<>();
        var statement = "Select * from auths";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    String user, auth;
                    while (rs.next()) {
                        user = rs.getString("user");
                        auth = rs.getString("authToken");
                        auths.add(new AuthData(user, auth));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format
                    ("Unable to read data: %s", e.getMessage()));
        }
        return auths.size();
    }

    @Override
    public AuthData createAuth(AuthData auth) throws DataAccessException {
        configureDB();
        var statement = "INSERT INTO auths (user, authToken) VALUES (?, ?)";
        executeUpdate(statement, auth.username(), auth.authToken());
        return new AuthData(auth.username(), auth.authToken());
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        configureDB();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM auths WHERE authToken=?";
            try (var pStatement = conn.prepareStatement(statement)) {
                pStatement.setString(1, authToken);
                try (var resultSet = pStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return new AuthData(resultSet.getString("user"),
                                resultSet.getString("authToken"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        configureDB();
        var statement = "DELETE FROM auths WHERE authToken=?";
        executeUpdate(statement, authToken);
    }

    @Override
    public void clear() throws DataAccessException {
        configureDB();
        var statement = "TRUNCATE auths";
        executeUpdate(statement);
    }
}
