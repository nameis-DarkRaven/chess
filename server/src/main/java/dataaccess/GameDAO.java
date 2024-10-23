package dataaccess;
import chess.ChessGame;
import model.GameData;

import java.util.Collection;

public interface GameDAO {
    public void createGame(String gameName);
    public GameData getGame(int gameID) throws DataAccessException;
    public Collection<GameData> listGames(String authToken);
    public void updateGame(int gameID, GameData game) throws DataAccessException;
    public void clear();
}
