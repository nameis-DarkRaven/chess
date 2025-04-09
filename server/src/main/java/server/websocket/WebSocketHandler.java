package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import exceptions.*;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import server.GameService;
import server.UserService;
import websocket.commands.*;
import websocket.messages.*;

import org.eclipse.jetty.websocket.api.*;

import java.io.IOException;

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
                }
                case MAKE_MOVE: {
                    MakeMoveCommand makeMoveCommand = new Gson().fromJson(msg, MakeMoveCommand.class);
                    makeMove(username, session, makeMoveCommand);
                }
                case LEAVE: {
                    LeaveCommand leaveCommand = new Gson().fromJson(msg, LeaveCommand.class);
//                    connect(session, username, leaveCommand);
                }
                case RESIGN: {
                    ResignCommand resignCommand = new Gson().fromJson(msg, ResignCommand.class);
//                    connect(session, username, (ResignCommand) command);
                }
            }
//deserialize twice, once to get type, second time to get other stuffs
            saveSession(command.getGameID(), session);
        } catch (UnauthorizedException ex) {
            sendMessage(session, new ErrorMessage("Error: unauthorized"));
        } catch (Exception ex) {
            ex.printStackTrace();
            sendMessage(session, new ErrorMessage("Error: " + ex.getMessage()));
        }
    }

    private void saveSession(int gameID, Session session) {
        //session is added to our connection manager
    }

    private void sendMessage(Session session, ErrorMessage error) {
        try {
            session.getRemote().sendString(new Gson().toJson(error));
        } catch (IOException e) {
            System.out.println("Error: unable to send message");
        }
    }

    private void connect(String username, Session session, ConnectCommand command)
            throws IOException {
        Connection connection = new Connection(username, session);
        try {
            connection = connections.add(command.getGameID(), username, session);
            int gameID = command.getGameID();
            ChessGame game = gameService.getGame(gameID).game();
            LoadGameMessage loadGameMessage = new LoadGameMessage(game);
            sendMessage(session, loadGameMessage);
            ChessGame.TeamColor color = game.getTeamTurn();
            var message = String.format("%s is now playing as %s", username, color.toString());
            var notification = new NotificationMessage(message);
            connections.broadcast(command.getGameID(), username, notification);
        } catch (DataAccessException e) {
            var remote = session.getRemote();
            connection.sendError(remote, e.getMessage());
        }
    }

    private void makeMove(String username, Session session, MakeMoveCommand command)
            throws IOException, UnauthorizedException {
        Connection connection = new Connection(username, session);
        try {
            int gameID = command.getGameID();
            GameData gameData = gameService.getGame(gameID);
            if (!username.equals(gameData.blackUsername()) || !username.equals(gameData.whiteUsername())) {
                throw new UnauthorizedException("You must be a player to make a move.");
            }
            connection = connections.add(command.getGameID(), username, session);
            ChessMove move = command.move();
            ChessGame game = gameData.game();
            LoadGameMessage loadGameMessage = new LoadGameMessage(game);
            sendMessage(session, loadGameMessage);
            var message = String.format("%s moved %s to %s", username, move.getStartPosition(), move.getEndPosition());
            var notification = new NotificationMessage(message);
            connections.broadcast(command.getGameID(), username, notification);
        } catch (DataAccessException e) {
            var remote = session.getRemote();
            connection.sendError(remote, e.getMessage());
        }
    }

    private void leave(String username, LeaveCommand command) throws IOException {
        connections.remove(command.getGameID(), username);
        var message = String.format("%s left the game.", username);
        var notification = new NotificationMessage(message);
        connections.broadcast(command.getGameID(), username, notification);
    }

    private void resign(String username, ResignCommand command) throws IOException {
        connections.remove(command.getGameID(), username);
        var message = String.format("%s resigned.", username);
        var notification = new NotificationMessage(message);
        connections.broadcast(command.getGameID(), username, notification);
    }
}
