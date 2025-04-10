package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import dataaccess.GameDAO;
import exceptions.*;
import model.GameData;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import server.GameService;
import server.UserService;
import websocket.commands.*;
import websocket.messages.*;

import org.eclipse.jetty.websocket.api.*;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    private final UserService userService;
    private final GameService gameService;
    private final ConnectionsManager connections = new ConnectionsManager();

    public WebSocketHandler(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String msg) {
        try {
            UserGameCommand command = new Gson().fromJson(msg, UserGameCommand.class);
            String username = userService.getAuth(command.getAuthToken()).username();
            switch (command.getCommandType()) {
                case CONNECT: {
                    ConnectCommand connectCommand = new Gson().fromJson(msg, ConnectCommand.class);
                    connect(username, session, connectCommand);
                    break;
                }
                case MAKE_MOVE: {
                    MakeMoveCommand makeMoveCommand = new Gson().fromJson(msg, MakeMoveCommand.class);
                    makeMove(username, session, makeMoveCommand);
                    break;
                }
                case LEAVE: {
                    LeaveCommand leaveCommand = new Gson().fromJson(msg, LeaveCommand.class);
                    leave(username, leaveCommand);
                    break;
                }
                case RESIGN: {
                    ResignCommand resignCommand = new Gson().fromJson(msg, ResignCommand.class);
                    resign(username, session, resignCommand);
                    break;
                }
            }
        } catch (UnauthorizedException ex) {
            sendMessage(session, new ErrorMessage("Error: unauthorized"));
        } catch (Exception ex) {
            ex.printStackTrace();
            sendMessage(session, new ErrorMessage("Error: " + ex.getMessage()));
        }
    }

    private void sendMessage(Session session, ServerMessage message) {
        try {
            var msg = new Gson().toJson(message);
            session.getRemote().sendString(msg);
        } catch (IOException e) {
            System.out.println("Error: unable to send message");
        }
    }

    private void broadcast(String message, int id, String user) throws IOException {
        var notification = new NotificationMessage(message);
        connections.broadcast(id, user, notification);
    }

    private void connect(String username, Session session, ConnectCommand command)
            throws IOException {
        Connection connection = new Connection(username, session);
        try {
            int gameID = command.getGameID();
            GameData gameData = gameService.getGame(gameID);
            ChessGame game = gameData.game();
            connection = connections.add(command.getGameID(), username, session);
            LoadGameMessage loadGameMessage = new LoadGameMessage(game);
            sendMessage(session, loadGameMessage);
            ChessGame.TeamColor color = game.getTeamTurn();
            var message = String.format("%s is now playing as %s", username, color.toString());
            broadcast(message, gameID, username);
        } catch (DataAccessException e) {
            var remote = session.getRemote();
            connection.sendError(remote, e.getMessage());
        }
    }

    private void makeMove(String username, Session session, MakeMoveCommand command)
            throws IOException {
        Connection connection = new Connection(username, session);
        try {
            int gameID = command.getGameID();
            GameDAO games = gameService.getGames();
            GameData gameData = gameService.getGame(gameID);
            if (!username.equals(gameData.blackUsername()) && !username.equals(gameData.whiteUsername())) {
                throw new UnauthorizedException("You must be a player to make a move.");
            }
            ChessGame game = gameData.game();
            ChessMove move = command.move();
            String opponent = switch (game.getTeamTurn()) {
                case WHITE -> gameData.blackUsername();
                case BLACK -> gameData.whiteUsername();
            };
            if (opponent.equals(username)){
                String userTurn = switch (game.getTeamTurn()) {
                    case WHITE -> gameData.whiteUsername();
                    case BLACK -> gameData.blackUsername();
                };
                throw new BadRequestException(String.format("It is %s's turn.", userTurn));
            }
            if (game.isGameOver()) {
                throw new BadRequestException("Game is over. No moves can be made.");
            }
            game.makeMove(move);
            gameData = new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
            games.updateGame(gameID, gameData);
            connection = connections.add(command.getGameID(), username, session);
            LoadGameMessage loadGameMessage = new LoadGameMessage(game);
            connections.broadcast(gameID, "", loadGameMessage);
            var message = String.format("%s moved %s to %s", username, move.getStartPosition(), move.getEndPosition());
            broadcast(message, gameID, username);
            if (game.isInStalemate(game.getTeamTurn())) {
                message = String.format("%s is in stalemate. %s wins!", opponent, username);
                broadcast(message, gameID, "");
                game.setGameOver();
            } else if (game.isInCheckmate(game.getTeamTurn())) {
                message = String.format("%s is in checkmate. %s wins!", opponent, username);
                broadcast(message, gameID, "");
                game.setGameOver();
            } else if (game.isInCheck(game.getTeamTurn())) {
                message = String.format("%s is in check.", opponent);
                broadcast(message, gameID, "");
            }
        } catch (Exception e) {
            var remote = session.getRemote();
            connection.sendError(remote, e.getMessage());
        }
    }

    private void leave(String username, LeaveCommand command) throws IOException, DataAccessException {
        connections.remove(command.getGameID(), username);
        GameData gameData = gameService.getGame(command.getGameID());
        if (!(gameData.blackUsername() == null) && gameData.blackUsername().equals(username)){
            gameData = new GameData(gameData.gameID(), gameData.whiteUsername(),
                    null, gameData.gameName(), gameData.game());
        }
        else if (!(gameData.whiteUsername() == null) && gameData.whiteUsername().equals(username)){
            gameData = new GameData(gameData.gameID(), null,
                    gameData.blackUsername(), gameData.gameName(), gameData.game());
        }
        GameDAO games = gameService.getGames();
        games.updateGame(command.getGameID(), gameData);
        var message = String.format("%s left the game.", username);
        broadcast(message, command.getGameID(), username);
    }

    private void resign(String username, Session session, ResignCommand command) throws IOException {
        try {
            GameDAO games = gameService.getGames();
            int gameID = command.getGameID();
            GameData gameData = gameService.getGame(gameID);
            if (!username.equals(gameData.blackUsername()) && !username.equals(gameData.whiteUsername())) {
                throw new UnauthorizedException("You must be a player to resign.");
            }
            ChessGame game = gameData.game();
            if (game.isGameOver()){
                throw new BadRequestException("Game is already over.");
            }
            game.setGameOver();

            String opponent = switch (game.getTeamTurn()) {
                case WHITE -> gameData.whiteUsername();
                case BLACK -> gameData.blackUsername();
            };
            var message = String.format("%s resigned. %s wins!", username, opponent);
            broadcast(message, gameID, "");
            games.updateGame(gameID, gameData);
        } catch (Exception e){
            Connection connection = connections.getConnection(command.getGameID(), username);
            var remote = session.getRemote();
            connection.sendError(remote, e.getMessage());
        }
    }
}
