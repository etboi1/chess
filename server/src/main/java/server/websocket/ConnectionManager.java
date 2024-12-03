package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public void add(String connectedUser, Session session, Integer gameID) {
        var connection = new Connection(connectedUser, session, gameID);
        connections.put(connectedUser, connection);
    }

    public void remove(String connectedUser) { connections.remove(connectedUser); }

    public void broadcast(String rootClient, Integer rootGameID, ServerMessage serverMessage) throws IOException {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.values()) {
            if (c.session.isOpen() && c.gameID.equals(rootGameID)) {
                if (!c.connectedUser.equals(rootClient)) {
                    c.send(new Gson().toJson(serverMessage));
                }
            } else if (!c.session.isOpen()){
                removeList.add(c);
            }
        }

        for (var c : removeList) {
            connections.remove(c.connectedUser);
        }
    }
}
