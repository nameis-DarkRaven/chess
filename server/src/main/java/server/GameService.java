package server;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.GameData;
import java.util.Collection;

public class GameService {
    private AuthDAO auths;
    private GameDAO games;

    public GameService(AuthDAO auths, GameDAO games) {
        this.auths = auths;
        this.games = games;
    }

    public ListGamesResult listGames(ListGamesRequest request) throws DataAccessException{
        try{
            AuthData auth = auths.getAuth(request.authToken());
            Collection<GameData> gamesList = games.listGames(auth.authToken());
            return new ListGamesResult(gamesList);
        }catch(DataAccessException e){
            throw new DataAccessException(e.getMessage());
        }
    }
    public CreateGameResult createGame(CreateGameRequest request)throws DataAccessException{
        try{
            AuthData auth = auths.getAuth(request.authToken());
            int gameID = games.createGame(request.gameName());
            return new CreateGameResult(gameID);
        }catch(DataAccessException e){
            throw new DataAccessException(e.getMessage());
        }
    }
    public JoinGameResult joinGame(JoinGameRequest request) throws DataAccessException{
        try{
            AuthData auth = auths.getAuth(request.authToken());
            GameData game = games.getGame(request.gameID());
            games.updateGame(game.gameID(), game);
            return new JoinGameResult();
        }catch(DataAccessException e){
            throw new DataAccessException(e.getMessage());
        }
    }

}
