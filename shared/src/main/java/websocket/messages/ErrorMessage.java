package websocket.messages;

public class ErrorMessage extends ServerMessage{
    ServerMessage.ServerMessageType serverMessageType;
    String errorMessage;

    public ErrorMessage(String errorMessage) {
        super(ServerMessageType.ERROR);
        this.errorMessage = errorMessage;
    }
}
