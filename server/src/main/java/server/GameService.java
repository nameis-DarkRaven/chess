package server;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import model.*;
import requests.*;
import results.CreateGameResult;
import results.JoinGameResult;
import results.ListGamesResult;

import java.util.Collection;

public class GameService {
    private final AuthDAO auths;
    private final GameDAO games;
    private final UserDAO users;

    public GameService(AuthDAO auths, GameDAO games, UserDAO users) {
        this.auths = auths;
        this.games = games;
        this.users = users;
    }

    public ListGamesResult listGames(ListGamesRequest request) throws DataAccessException {
        try {
            AuthData auth = auths.getAuth(request.authToken());
            Collection<GameData> gamesList = games.listGames(auth.authToken());
            return new ListGamesResult(gamesList);
        } catch (DataAccessException e) {
            throw new UnauthorizedException("Error: Unauthorized access.");
        }
    }

    public CreateGameResult createGame(CreateGameRequest request) throws DataAccessException {
        try {
            AuthData auth = auths.getAuth(request.authToken());
            int gameID = games.createGame(request.gameName());
            return new CreateGameResult(gameID);
        } catch (DataAccessException e) {
            throw new UnauthorizedException("Error: Unauthorized access.");
        }
    }

    public JoinGameResult joinGame(JoinGameRequest request) throws DataAccessException {
        try {
            if (games.getGame(request.gameID()) == null) {
                throw new BadRequestException("Error: Invalid request.");
            }
            AuthData auth = auths.getAuth(request.authToken());
            UserData user = users.getUser(auth.username());
            GameData game = games.getGame(request.gameID());
            if (request.playerColor() == ChessGame.TeamColor.BLACK) {
                if (game.blackUsername() == null) {
                    games.updateGame(game.gameID(), new GameData(game.gameID(), game.whiteUsername(), user.username(), game.gameName(), game.game()));
                } else {
                    throw new AlreadyTakenException("Error: Black is already taken.");
                }
            } else if (request.playerColor() == ChessGame.TeamColor.WHITE) {
                if (game.whiteUsername() == null) {
                    games.updateGame(game.gameID(), new GameData(game.gameID(), user.username(), game.blackUsername(), game.gameName(), game.game()));
                } else {
                    throw new AlreadyTakenException("Error: Black is already taken.");
                }
            } else {
                throw new BadRequestException("Error: Invalid request.");
            }
            return new JoinGameResult();
        } catch (DataAccessException e) {
            throw new DataAccessException("Error: Unauthorized access.");
        }
    }

}
