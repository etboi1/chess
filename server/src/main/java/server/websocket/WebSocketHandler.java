package server.websocket;


import chess.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import exception.ResponseException;
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

    /**
     * Main websocket user command methods
     */
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

    private void makeMove(MakeMoveCommand command, Session session) throws Exception {
        //Check for authorization
        if (isUnautherized(authDataAccess.getAuth(command.getAuthToken()), session)) {
            return;
        }
        var rootClient = authDataAccess.getAuth(command.getAuthToken()).username();
        var currentGame = gameDataAccess.getGame(command.getGameID());
        //Check that the game exists in the database
        if (gameDoesNotExist(currentGame, session)) {
            return;
        }
        //Check if the game has been marked as finished
        if (currentGame.game().getGameState() == ChessGame.GameState.FINISHED) {
            sendErrorMessage("Error: no moves can be made - game is over.", session);
            return;
        }
        var playerColor = getPlayerColor(rootClient, currentGame);
        ChessGame.TeamColor turn = currentGame.game().getTeamTurn();
        //Make sure the player is moving their own color and that they aren't an observer
        if ((playerColor.equals("white") && turn.equals(ChessGame.TeamColor.BLACK)) |
                (playerColor.equals("black") && turn.equals(ChessGame.TeamColor.WHITE))) {
            sendErrorMessage("Error: It is not your turn.", session);
            return;
        } else if (playerColor.equals("observer")) {
            sendErrorMessage("Error: You cannot make moves as an observer.", session);
            return;
        }
        //Check if a pawn promotion should occur and if a promotion piece has been supplied
        if (violatesPromotionRules(command.getMove(), currentGame.game().getBoard(), playerColor)) {
            sendErrorMessage("Error: when moving a pawn to the opposite end of the board, " +
                    "you must specify a promotion piece.", session);
            return;
        }

        ChessPiece piece = currentGame.game().getBoard().getPiece(command.getMove().getStartPosition());
        try {
            currentGame.game().makeMove(command.getMove());
        } catch (InvalidMoveException ex) {
            sendErrorMessage("Error: You have attempted an invalid move." +
                    "Make sure it is your turn and that you typed in the intended coordinates.", session);
            return;
        }
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

        ChessGame.TeamColor oppositePlayerColor = currentGame.game().getTeamTurn();
        checkForGameEndingMoves(currentGame, oppositePlayerColor, session);
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

    private void resign(String authToken, Integer gameID, Session session) {

    }

    /**
     * Helper methods for each of the 4 main methods
     */
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

    /**
     * Helper methods for connect function
     */
    private static NotificationMessage generateConnectNotification(String rootUser, GameData targetGame) {
        String message = String.format("%s has joined the game as an observer!", rootUser);
        if (targetGame.whiteUsername().equals(rootUser)) {
            message = String.format("%s has joined the game as %s!", rootUser, "white");
        } else if (targetGame.blackUsername().equals(rootUser)) {
            message = String.format("%s has joined the game as %s!", rootUser, "black");
        }
        return new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
    }

    /**
     * Helper methods for makeMove function
     */
    private boolean violatesPromotionRules(ChessMove move, ChessBoard board, String playerColor) throws Exception{
        ChessPiece piece = board.getPiece(move.getStartPosition());
        int startRow = move.getStartPosition().getRow();
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            if ((startRow == 7 && Objects.equals(playerColor, "white")) |
                    (startRow == 2 && Objects.equals(playerColor, "black"))) {
                return move.getPromotionPiece() == null;
            }
        }
        return false;
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

    private String getPlayerColor(String username, GameData game) {
        if (Objects.equals(username, game.whiteUsername())) {
            return "white";
        } else if (Objects.equals(username, game.blackUsername())) {
            return "black";
        }
        return "observer";
    }

    private String convertColumn(int col) {
        String[] letters = {"a", "b", "c", "d", "e", "f", "g", "h"};
        return letters[col - 1];
    }

    private void checkForGameEndingMoves(GameData gameData, ChessGame.TeamColor oppositePlayerColor, Session session) throws Exception {
        String oppositeUsername = (oppositePlayerColor == ChessGame.TeamColor.WHITE) ?
                gameData.whiteUsername() : gameData.blackUsername();
        NotificationMessage notification = null;
        if (gameData.game().isInCheckmate(oppositePlayerColor)) {
            notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    String.format("%s is in checkmate!", oppositeUsername));
        } else if (gameData.game().isInStalemate(oppositePlayerColor)) {
            notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    "The game has ended in a stalemate!");
        } else if (gameData.game().isInCheck(oppositePlayerColor)) {
            notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    String.format("%s is in check!", oppositeUsername));
        }
        if (notification != null) {
            connections.broadcast(null, gameData.gameID(), notification);
        }
    }

    /**
     * Helper methods for leave function
     */
    private void updateGameUsers(String rootClient, Integer gameID, GameData currentGame) throws Exception {
        if (Objects.equals(rootClient, currentGame.whiteUsername())) {
            GameData updatedGame = new GameData(gameID, null, currentGame.blackUsername(), currentGame.gameName(), currentGame.game());
            gameDataAccess.updateGame(gameID, updatedGame);
        } else if (Objects.equals(rootClient, currentGame.blackUsername())) {
            GameData updatedGame = new GameData(gameID, currentGame.whiteUsername(), null, currentGame.gameName(), currentGame.game());
            gameDataAccess.updateGame(gameID, updatedGame);
        }
    }
}
