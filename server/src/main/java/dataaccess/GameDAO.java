package dataaccess;
import chess.ChessGame;

public interface GameDAO {
    public void createGame(String gameName);
    public ChessGame getGame(int gameID);

}
