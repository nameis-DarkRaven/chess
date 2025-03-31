package dataaccess;
import exceptions.DataAccessException;
import model.GameData;

import java.util.Collection;


public interface GameDAO {
    public int createGame(String gameName) throws DataAccessException;
    public GameData getGame(int gameID) throws DataAccessException;
    public Collection<GameData> listGames() throws DataAccessException;
    public void updateGame(int gameID, GameData game) throws DataAccessException;
    public void clear()throws DataAccessException;
}
