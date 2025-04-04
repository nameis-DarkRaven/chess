package dataaccess;

import chess.ChessGame;
import exceptions.DataAccessException;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;

public class MemoryGameDAO implements GameDAO {
    private Collection<GameData> gameData = new ArrayList<>();

    private int createGameID() {
        return gameData.size() + 1;
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        int gameID = createGameID();
        gameData.add(new GameData(gameID, null, null, gameName, new ChessGame()));
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        if (gameData == null) {
            return null;
        }
        for (GameData game : gameData) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        return null;
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return gameData;
    }

    @Override
    public void updateGame(int gameID, GameData game) throws DataAccessException {
        if (gameData != null) {
            GameData oldGame = getGame(gameID);
            gameData.remove(oldGame);
            gameData.add(game);
        } else {
            throw new DataAccessException("Game does not exist.");
        }
    }

    @Override
    public void clear() throws DataAccessException {
        if (gameData != null) {
            gameData.clear();
        }
    }


}
