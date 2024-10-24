package dataaccess;
import chess.ChessGame;
import model.GameData;

import java.util.Collection;

public interface GameDAO {
    public void createGame(String gameName) throws DataAccessException;
    public GameData getGame(int gameID) throws DataAccessException;
    public Collection<GameData> listGames(String authToken) throws DataAccessException;
    public void updateGame(int gameID, GameData game) throws DataAccessException;
    public void clear()throws DataAccessException;
}
