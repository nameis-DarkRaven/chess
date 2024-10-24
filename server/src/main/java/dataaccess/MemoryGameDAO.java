package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;

public class MemoryGameDAO implements GameDAO {
    private Collection<GameData> gameData = new ArrayList<>();
    private ArrayList<Integer> ids = new ArrayList<>();

    private int createGameID() {
        return ids.get(-1) + 1;
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        int gameID = createGameID();
        gameData.add(new GameData(gameID, null, null, gameName, new ChessGame()));
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        if (gameData == null){return null;}
        for (GameData game : gameData) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        throw new DataAccessException("Game does not exist.");
    }

    @Override
    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        return gameData;
    }

    @Override
    public void updateGame(int gameID, GameData game) throws DataAccessException {
        if (gameData != null){
        gameData.remove(game);
        gameData.add(new GameData(gameID, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game()));
    } else{throw new DataAccessException("Game does not exist.");}
    }

    @Override
    public void clear() throws DataAccessException {
        if (gameData != null) {
            gameData.clear();
        }
        if (ids != null) {
            ids.clear();
        }
    }


}
