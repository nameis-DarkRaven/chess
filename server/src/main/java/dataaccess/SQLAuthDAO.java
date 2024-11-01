package dataaccess;

import com.google.gson.Gson;
import com.mysql.cj.exceptions.DataReadException;
import model.AuthData;
import model.UserData;

import java.sql.ResultSet;
import java.sql.SQLException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class SQLAuthDAO implements AuthDAO {

    public SQLAuthDAO() throws DataAccessException {
        configureDatabase();
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
              `authToken` varchar(256) NOT NULL,
              `json` TEXT DEFAULT NULL,
              PRIMARY KEY (`authToken`),
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

    private AuthData readAuth(ResultSet resultSet) throws SQLException{
        var json = resultSet.getString("json");
        return new Gson().fromJson(json, AuthData.class);
    }

    @Override
    public AuthData createAuth(AuthData auth) throws DataAccessException {
        var statement = "INSERT INTO auths (user, password, json) VALUES (?, ?, ?)";
        var json = new Gson().toJson(auth);
        executeUpdate(statement, auth.username(), auth.authToken(), json);
        return new AuthData(auth.username(), auth.authToken());
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, json FROM users WHERE username=?";
            try (var pStatement = conn.prepareStatement(statement)) {
//                pStatement.setString(1, id);
                try (var resultSet = pStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return readAuth(resultSet);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        var statement = "DELETE FROM auths WHERE authToken=?";
        executeUpdate(statement, authToken);
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE auths";
        executeUpdate(statement);
    }
}
