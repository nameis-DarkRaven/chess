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

    private void connect(String username, Session session, ConnectCommand command) throws IOException, BadRequestException {
        try {
            connections.add(username, session);
            int gameID = command.getGameID();
            ChessGame.TeamColor color = gameService.getGame(gameID).getTeamTurn();
            var message = String.format("%s is now playing as %s", username, color.toString());
            var notification = new NotificationMessage(message);
            connections.broadcast(username, notification);
        } catch (DataAccessException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private void makeMove(String username, Session session, MakeMoveCommand command) throws IOException {
        ChessMove move = command.move();
        var message = String.format("%s moved %s to %s", username, move.getStartPosition(), move.getEndPosition());
        var notification = new NotificationMessage(message);
        connections.broadcast(username, notification);
    }

    public void makeNoise(String petName, String sound) throws BadRequestException {
        try {
            var message = String.format("%s says %s", petName, sound);
            var notification = new NotificationMessage(message);
            connections.broadcast("", notification);
        } catch (Exception ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }
}
