package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;

public class MemoryGameDAO implements GameDAO {
    private Collection<GameData> gameData;
    private Collection<Integer> ids;

    private int createGameID(){
        int id = 1;
        while (ids.contains(id)){
            id++;
        }
        return id;
    }

    @Override
    public int createGame(String gameName) throws DataAccessException{
        int gameID = createGameID();
        gameData.add(new GameData(gameID, null, null, gameName, new ChessGame()));
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        for(GameData game: gameData){
            if (game.gameID() == gameID){return game;}
        }
        throw new DataAccessException("Game does not exist.");
    }

    @Override
    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        return gameData;
    }

    @Override
    public void updateGame(int gameID, GameData game)throws DataAccessException{
        gameData.remove(game);
        gameData.add(new GameData(gameID, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game()));
    }

    @Override
    public void clear() throws DataAccessException{
        gameData.clear();
        ids.clear();
    }


}
