package ui;

import chess.ChessGame;
import websocket.messages.ServerMessage;

public interface NotificationHandler {
    void notify(ServerMessage notification, String currentUser);
}
