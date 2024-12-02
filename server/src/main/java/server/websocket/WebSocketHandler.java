package server.websocket;


import chess.ChessGame;
import chess.ChessPiece;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.MakeMoveCommand;
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
        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();

        // Get the type field from the JSON
        String commandType = jsonObject.get("commandType").getAsString();
        UserGameCommand command;
        if (Objects.equals(commandType, "MAKE_MOVE")) {
            MakeMoveCommand moveCommand = new Gson().fromJson(message, MakeMoveCommand.class);
            makeMove(moveCommand, session);
        } else {
            command = new Gson().fromJson(message, UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> connect(command.getAuthToken(), command.getGameID(), session);
                case LEAVE -> leave(command.getAuthToken(), command.getGameID(), session);
                case RESIGN -> resign(command.getAuthToken(), command.getGameID(), session);
            }
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
        var notificationMessage = generateConnectNotification(rootUser, targetGame);
        connections.broadcast(rootUser, gameID, notificationMessage);

        //Return a load game message to the root client
        var loadGameMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, targetGame);
        session.getRemote().sendString(new Gson().toJson(loadGameMessage));
    }

    private static NotificationMessage generateConnectNotification(String rootUser, GameData targetGame) {
        String message = String.format("%s has joined the game as an observer!", rootUser);
        if (targetGame.whiteUsername().equals(rootUser)) {
            message = String.format("%s has joined the game as %s!", rootUser, "white");
        } else if (targetGame.blackUsername().equals(rootUser)) {
            message = String.format("%s has joined the game as %s!", rootUser, "black");
        }
        return new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
    }

    private void makeMove(MakeMoveCommand command, Session session) throws Exception{
        //Check for authorization, that the game exists, and that they are not just an observer
        if (isUnautherized(authDataAccess.getAuth(command.getAuthToken()), session)) {
            return;
        }
        var rootClient = authDataAccess.getAuth(command.getAuthToken()).username();
        var currentGame = gameDataAccess.getGame(command.getGameID());
        if (gameDoesNotExist(currentGame, session)) {
            return;
        }
        var playerColor = getPlayerColor(rootClient, currentGame);
        ChessGame.TeamColor turn = currentGame.game().getTeamTurn();
        if ((playerColor.equals("white") && turn.equals(ChessGame.TeamColor.BLACK)) |
                (playerColor.equals("black") && turn.equals(ChessGame.TeamColor.WHITE))) {
            sendErrorMessage("Error: It is not your turn.", session);
            return;
        } else if (playerColor.equals("observer")) {
            sendErrorMessage("Error: You cannot make moves as an observer.", session);
            return;
        }
        try {
            ChessPiece piece = currentGame.game().getBoard().getPiece(command.getMove().getStartPosition());
            currentGame.game().makeMove(command.getMove());
            //Update board in database
            gameDataAccess.updateGame(command.getGameID(), currentGame);
            //Send the LOAD_GAME server message to all other users involved in game
            var loadGameMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, currentGame);
            connections.broadcast(rootClient, command.getGameID(), loadGameMessage);
            //Send the notification to all others involved in game
            var notification = generateMoveNotification(command, piece, rootClient);
            connections.broadcast(rootClient, command.getGameID(), notification);
            //Send the LOAD_GAME message back to the root user
            session.getRemote().sendString(new Gson().toJson(loadGameMessage));
        } catch (InvalidMoveException ex) {
            sendErrorMessage("Error: You have attempted an invalid move." +
                    "Make sure it is your turn and that you typed in the intended coordinates.", session);
            return;
        }
    }

    private NotificationMessage generateMoveNotification(MakeMoveCommand command, ChessPiece piece, String rootClient) {
        ChessPiece.PieceType pieceType = piece.getPieceType();
        String startRow = String.valueOf(command.getMove().getStartPosition().getRow());
        String startCol = convertColumn(command.getMove().getStartPosition().getColumn());
        String endRow = String.valueOf(command.getMove().getEndPosition().getRow());
        String endCol = convertColumn(command.getMove().getEndPosition().getColumn());
        var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                String.format("%s moved the %s at %s%s to %s%s.",
                        rootClient, pieceType, startRow, startCol, endRow, endCol));
        return notification;
    }

    private String convertColumn(int col) {
        String[] letters = {"a", "b", "c", "d", "e", "f", "g", "h"};
        return letters[col];
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
        updateGameUsers(rootClient, gameID, targetGame);
        connections.remove(rootClient);

        var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                String.format("%s has left the game.", rootClient));
        connections.broadcast(rootClient, gameID, notification);
    }

    private void updateGameUsers(String rootClient, Integer gameID, GameData currentGame) throws Exception {
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
            sendErrorMessage("Error: unauthorized", session);
            return true;
        }
        return false;
    }

    private boolean gameDoesNotExist(GameData targetGame, Session session) throws Exception {
        if (targetGame == null) {
            sendErrorMessage("Error: No game exists with provided gameID.", session);
            return true;
        }
        return false;
    }

    private void sendErrorMessage(String message, Session session) throws Exception{
        var errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, message);
        session.getRemote().sendString(new Gson().toJson(errorMessage));
    }

    private String getPlayerColor(String username, GameData game) {
        if (Objects.equals(username, game.whiteUsername())) {
            return "white";
        } else if (Objects.equals(username, game.blackUsername())) {
            return "black";
        }
        return "observer";
    }
}
