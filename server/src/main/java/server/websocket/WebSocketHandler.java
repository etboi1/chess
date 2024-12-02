package server.websocket;


import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.Objects;

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
        if (isUnautherized(rootUserAuth, session)) {
            return;
        }
        var rootUser = rootUserAuth.username();

        var targetGame = gameDataAccess.getGame(gameID);
        if (gameDoesNotExist(targetGame, session)) {
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

    private void leave(String authToken, Integer gameID, Session session) throws Exception {
        //Ensure the user is authorized and provides a valid gameID
        if (isUnautherized(authDataAccess.getAuth(authToken), session)) {
            return;
        }
        var rootClient = authDataAccess.getAuth(authToken).username();
        var targetGame = gameDataAccess.getGame(gameID);
        if (gameDoesNotExist(targetGame, session)) {
            return;
        }
        updateGame(rootClient, gameID, targetGame);
        connections.remove(rootClient);

        var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                String.format("%s has left the game!", rootClient));
        connections.broadcast(rootClient, gameID, notification);
    }

    private void updateGame(String rootClient, Integer gameID, GameData currentGame) throws Exception {
        if (Objects.equals(rootClient, currentGame.whiteUsername())) {
            GameData updatedGame = new GameData(gameID, null, currentGame.blackUsername(), currentGame.gameName(), currentGame.game());
            gameDataAccess.updateGame(gameID, updatedGame);
        } else if (Objects.equals(rootClient, currentGame.blackUsername())) {
            GameData updatedGame = new GameData(gameID, currentGame.whiteUsername(), null, currentGame.gameName(), currentGame.game());
            gameDataAccess.updateGame(gameID, updatedGame);
        }
    }

    private void resign(String authToken, Integer gameID, Session session) {

    }

    private boolean isUnautherized(AuthData authData, Session session) throws Exception {
        if (authData == null) {
            var errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                    "Error: unauthorized");
            session.getRemote().sendString(new Gson().toJson(errorMessage));
            return true;
        }
        return false;
    }

    private boolean gameDoesNotExist(GameData targetGame, Session session) throws Exception {
        if (targetGame == null) {
            var errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                    "Error: No game exists with provided gameID.");
            session.getRemote().sendString(new Gson().toJson(errorMessage));
            return true;
        }
        return false;
    }
}
