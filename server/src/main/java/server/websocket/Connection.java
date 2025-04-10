package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ErrorMessage;

import java.io.IOException;

public class Connection {
    public String username;
    public Session session;

    public Connection(String username, Session session) {
        this.username = username;
        this.session = session;
    }

    public void send(String msg) throws IOException {
        session.getRemote().sendString(msg);
    }

    public void sendError(RemoteEndpoint remote, String message) throws IOException {
        ErrorMessage error = new ErrorMessage(message);
        remote.sendString(new Gson().toJson(error));
    }

}