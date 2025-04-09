package server;

import chess.ChessGame;
import dataaccess.*;
import exceptions.AlreadyTakenException;
import exceptions.BadRequestException;
import exceptions.DataAccessException;
import exceptions.UnauthorizedException;
import model.*;
import requests.*;
import results.*;

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

    public ListGamesResult listGames(ListGamesRequest request) throws UnauthorizedException {
        try {
            AuthData auth = auths.getAuth(request.authToken());
            if (auth == null) {
                throw new UnauthorizedException("Error: Unauthorized access.");
            }
            Collection<GameData> gamesList = games.listGames();
            return new ListGamesResult(gamesList);
        } catch (DataAccessException e) {
            throw new UnauthorizedException("Error: Unauthorized access.");
        }

    }

    public CreateGameResult createGame(CreateGameRequest request)
            throws DataAccessException, BadRequestException, UnauthorizedException {
        try {
            if (request.gameName() == null) {
                throw new BadRequestException("Error: Invalid game name.");
            }
            AuthData auth = auths.getAuth(request.authToken());
            if (auth == null) {
                throw new UnauthorizedException("Error: Unauthorized access.");
            }
            int gameID = games.createGame(request.gameName());
            return new CreateGameResult(gameID);
        } catch (DataAccessException e) {
            throw new UnauthorizedException("Error: Unauthorized access.");
        }
    }

    public JoinGameResult joinGame(JoinGameRequest request)
            throws DataAccessException, UnauthorizedException, BadRequestException, AlreadyTakenException {
        try {
            if (games.getGame(request.gameID()) == null) {
                throw new BadRequestException("Error: Invalid request.");
            }
            AuthData auth = auths.getAuth(request.authToken());
            if (auth == null) {
                throw new UnauthorizedException("Error: Unauthorized access.");
            }
            UserData user = users.getUser(auth.username());
            GameData game = games.getGame(request.gameID());
            if (request.playerColor() == ChessGame.TeamColor.BLACK) {
                if (game.blackUsername() == null) {
                    games.updateGame(game.gameID(), new GameData(game.gameID(), game.whiteUsername(),
                            user.username(), game.gameName(), game.game()));
                }
                else {
                    throw new AlreadyTakenException("Error: Another player has already taken that spot.");
                }
            } else if (request.playerColor() == ChessGame.TeamColor.WHITE) {
                if (game.whiteUsername() == null) {
                    games.updateGame(game.gameID(), new GameData(game.gameID(), user.username(),
                            game.blackUsername(), game.gameName(), game.game()));
                } else {
                    throw new AlreadyTakenException("Error: Another player has already taken that spot.");
                }
            } else {
                throw new BadRequestException("Error: Invalid request.");
            }
            return new JoinGameResult();
        } catch (DataAccessException e) {
            throw new UnauthorizedException("Error: Unauthorized access.");
        }
    }

    public ChessGame getGame(int gameID) throws DataAccessException {
        return games.getGame(gameID).game();
    }

}
