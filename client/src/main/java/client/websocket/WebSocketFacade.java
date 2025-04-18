package client.websocket;

import chess.ChessGame;
import chess.ChessMove;
import client.GameClient;
import com.google.gson.Gson;
import exceptions.BadRequestException;
import model.GameData;
import websocket.commands.*;
import websocket.messages.*;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class WebSocketFacade extends Endpoint {

    Session session;
    ChessGame game;
    GameClient gameClient;
    NotificationHandler notificationHandler;


    public WebSocketFacade(String url, NotificationHandler notificationHandler, GameClient gameClient)
            throws BadRequestException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
                    switch (serverMessage.getServerMessageType()){
                        case NOTIFICATION -> {
                            NotificationMessage notification = new Gson().fromJson(message, NotificationMessage.class);
                            notificationHandler.notify(new NotificationMessage(message));
                        }
                        case ERROR -> {
                            ErrorMessage error = new Gson().fromJson(message, ErrorMessage.class);
                            System.out.println(error.message());
                        }
                        case LOAD_GAME -> {
                            LoadGameMessage loadGame = new Gson().fromJson(message, LoadGameMessage.class);
                            game = loadGame.game();
                        }
                    }
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void connect(String authToken, int gameID) throws BadRequestException {
        try {
            var command = new ConnectCommand(authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
            gameClient.printBoard(game.getTeamTurn(), game);
        } catch (IOException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws BadRequestException {
        try {
            var command = new MakeMoveCommand(authToken, gameID, move);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
            gameClient.printBoard(game.getTeamTurn(), game);
        } catch (IOException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    public void leave(String authToken, int gameID) throws BadRequestException {
        try {
            var command = new LeaveCommand(authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
            this.session.close();
        } catch (IOException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    public void resign(String authToken, int gameID) throws BadRequestException {
        try {
            var command = new ResignCommand(authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
            this.session.close();
        } catch (IOException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

}

