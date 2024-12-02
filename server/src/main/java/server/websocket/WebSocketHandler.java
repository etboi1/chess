package server.websocket;


import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

@WebSocket
public class WebSocketHandler {

    private final AuthDAO authDataAccess;
    private final GameDAO gameDataAccess;
    private final ConnectionManager connections = new ConnectionManager();

    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDataAccess = authDAO;
        this.gameDataAccess = gameDAO;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connect(command.getAuthToken(), command.getGameID(), session);
            case MAKE_MOVE -> makeMove(command, session);
            case LEAVE -> leave(command.getAuthToken(), command.getGameID(), session);
            case RESIGN -> resign(command.getAuthToken(), command.getGameID(), session);
        }
    }

    private void connect(String authToken, Integer gameID, Session session) throws Exception {
        //Ensure that the root client is authorized and the game exists before saving the connection
        var rootUserAuth = authDataAccess.getAuth(authToken);
        if (rootUserAuth == null) {
            var errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                    "Error: unauthorized");
            session.getRemote().sendString(new Gson().toJson(errorMessage));
            return;
        }
        var rootUser = rootUserAuth.username();

        var targetGame = gameDataAccess.getGame(gameID);
        if (targetGame == null) {
            var errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                    "Error: No game exists with provided gameID.");
            session.getRemote().sendString(new Gson().toJson(errorMessage));
            return;
        }

        connections.add(rootUserAuth.username(), session, gameID);

        //Notify other players the root client joined the game
        var notificationMessage = getNotificationMessage(rootUser, targetGame);
        connections.broadcast(rootUser, gameID, notificationMessage);

        //Return a load game message to the root client
        var loadGameMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, targetGame);
        session.getRemote().sendString(new Gson().toJson(loadGameMessage));
    }

    private static NotificationMessage getNotificationMessage(String rootUser, GameData targetGame) {
        String message = String.format("%s has joined the game as an observer!", rootUser);
        if (targetGame.whiteUsername().equals(rootUser)) {
            message = String.format("%s has joined the game as %s!", rootUser, "white");
        } else if (targetGame.blackUsername().equals(rootUser)) {
            message = String.format("%s has joined the game as %s!", rootUser, "black");
        }
        return new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
    }

    private void makeMove(UserGameCommand command, Session session) {

    }

    private void leave(String authToken, Integer gameID, Session session) {

    }

    private void resign(String authToken, Integer gameID, Session session) {

    }
}
