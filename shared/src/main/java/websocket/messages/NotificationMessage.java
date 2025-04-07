package websocket.messages;

public class NotificationMessage extends ServerMessage {
    ServerMessage.ServerMessageType serverMessageType;
    String message;

    public NotificationMessage(ServerMessage.ServerMessageType type, String message) {
        super(type);
        this.message = message;
    }

    public String message(){
        return message;
    }
}
