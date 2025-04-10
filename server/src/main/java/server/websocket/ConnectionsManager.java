package server.websocket;

import com.google.gson.Gson;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsManager {
    public final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Connection>> connections = new ConcurrentHashMap<>();

    public Connection add(int gameID, String username, Session session) {
        var users = connections.get(gameID);
        if (users == null) {
            ConcurrentHashMap<String, Connection> connection = new ConcurrentHashMap<>();
            connection.put(username, new Connection(username, session));
            connections.put(gameID, connection);
        }
        ConcurrentHashMap<String, Connection> connection = connections.get(gameID);
        connection.put(username, new Connection(username, session));
        connections.put(gameID, connection);
        return connections.get(gameID).get(username);
    }

    public void remove(int gameID, String username) {
        var connection = connections.get(gameID);
        connection.remove(username);
    }

    public void broadcast(int gameID, String excludeUsername, ServerMessage message) throws IOException {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.get(gameID).values()) {
            if (c.session.isOpen()) {
                if (!c.username.equals(excludeUsername)) {
                    c.send(new Gson().toJson(message));
                }
            } else {
                removeList.add(c);
            }
        }
        // Clean up any connections that were left open.
        for (var c : removeList) {
            connections.remove(c.username);
        }
    }
    public Connection getConnection(int gameID, String username){
        return connections.get(gameID).get(username);
    }
}