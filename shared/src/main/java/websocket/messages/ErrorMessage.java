package websocket.messages;

public class ErrorMessage extends ServerMessage{
    ServerMessage.ServerMessageType serverMessageType;
    String errorMessage;

    public ErrorMessage(ServerMessage.ServerMessageType type, String errorMessage) {
        super(type);
        this.errorMessage = errorMessage;
    }
}
