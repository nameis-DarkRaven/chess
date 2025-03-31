package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import exceptions.DataAccessException;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;
import java.sql.*;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class SQLGameDAO implements GameDAO {

    private void configDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var ps = conn.prepareStatement(statement)) {
                    ps.executeUpdate();
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
        configDatabase();
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
        configDatabase();
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
    public Collection<GameData> listGames() throws DataAccessException {
        configDatabase();
        var gameList = new ArrayList<GameData>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM games";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int gameID = rs.getInt("gameID");
                        String blackUsername = rs.getString("blackUsername");
                        String whiteUsername = rs.getString("whiteUsername");
                        String gameName = rs.getString("gameName");
                        ChessGame game = new Gson().fromJson(rs.getString("game"), ChessGame.class);
                        gameList.add(new GameData(gameID, whiteUsername, blackUsername, gameName, game));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format
                    ("Unable to read data: %s", e.getMessage()));
        }
        return gameList;
    }

    @Override
    public void updateGame(int gameID, GameData game) throws DataAccessException {
        configDatabase();
        if (getGame(gameID) == null || gameID != game.gameID()) {
            throw new DataAccessException("Error: Incorrect game ID or game.");
        }
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
        configDatabase();
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
