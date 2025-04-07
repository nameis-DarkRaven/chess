package websocket.messages;

import chess.ChessGame;

public class LoadGameMessage extends ServerMessage{
    ServerMessageType serverMessageType;
    ChessGame game;

    public LoadGameMessage(ServerMessageType type, ChessGame game) {
        super(type);
        this.game = game;
    }

}
