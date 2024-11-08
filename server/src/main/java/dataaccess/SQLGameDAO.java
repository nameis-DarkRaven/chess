package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;
import java.sql.*;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class SQLGameDAO implements GameDAO {

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
            CREATE TABLE IF NOT EXISTS  games (
              `gameID` INT NOT NULL AUTO_INCREMENT,
              `whiteUsername` varchar(256) DEFAULT NULL,
              `blackUsername` varchar(256) DEFAULT NULL,
              `gameName` varchar(256) NOT NULL,
              `game` LONGTEXT NOT NULL,
              PRIMARY KEY (`gameID`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    @Override
    public int createGame(String gameName) throws DataAccessException {
        configureDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "INSERT INTO games (gameName, game) VALUES (?, ?)";
            try (var pStatement = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                pStatement.setString(1, gameName);
                var json = new Gson().toJson(new ChessGame());
                pStatement.setString(2, json);
                pStatement.executeUpdate();
                ResultSet rs = pStatement.getGeneratedKeys();
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format
                    ("Unable to create game: %s", e.getMessage()));
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        configureDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM games WHERE gameID=?";
            try (var pStatement = conn.prepareStatement(statement)) {
                pStatement.setInt(1, gameID);
                try (var resultSet = pStatement.executeQuery()) {
                    if (resultSet.next()) {
                        var json = resultSet.getString("game");
                        return new GameData(resultSet.getInt("gameID"),
                                resultSet.getString("whiteUsername"),
                                resultSet.getString("blackUsername"),
                                resultSet.getString("gameName"),
                                new Gson().fromJson(json, ChessGame.class));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format
                    ("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    @Override
    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        configureDatabase();
        var gameList = new ArrayList<GameData>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM auths";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    if (!rs.getString("authToken").equals(authToken)) {
                        throw new DataAccessException("Error: Unauthorized access.");
                    }
                    do {
                        if (!rs.getString("authToken").equals(authToken)) {
                            throw new DataAccessException("Error: Unauthorized access.");
                        }
                    }
                    while (rs.next());
                }
            }
            statement = "SELECT * FROM games";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    int gameID;
                    String blackUsername, whiteUsername, gameName;
                    ChessGame game;
                    while (rs.next()) {
                        gameID = rs.getInt("gameID");
                        blackUsername = rs.getString("blackUsername");
                        whiteUsername = rs.getString("whiteUsername");
                        gameName = rs.getString("gameName");
                        game = new Gson().fromJson(rs.getString("game"), ChessGame.class);
                        gameList.add(new GameData(gameID, whiteUsername, blackUsername, gameName, game));
                    }
                }
            }
        } catch (
                Exception e) {
            throw new DataAccessException(String.format
                    ("Unable to read data: %s", e.getMessage()));
        }
        return gameList;
    }

    @Override
    public void updateGame(int gameID, GameData game) throws DataAccessException {
        configureDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "Update games Set whiteUsername = ?, blackUsername = ?, game = ? WHERE gameID=?";
            try (var pStatement = conn.prepareStatement(statement)) {
                pStatement.setString(1, game.whiteUsername());
                pStatement.setString(2, game.blackUsername());
                var json = new Gson().toJson(game.game());
                pStatement.setString(3, json);
                pStatement.setInt(4, gameID);
                pStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        configureDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "TRUNCATE games";
            try (var pStatement = conn.prepareStatement(statement)) {
                pStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }


}
