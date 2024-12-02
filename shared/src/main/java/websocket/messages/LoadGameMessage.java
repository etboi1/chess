package websocket.messages;

import model.GameData;

public class LoadGameMessage extends ServerMessage {

    public final GameData game;

    public LoadGameMessage(ServerMessageType type, GameData gameData) {
        super(type);
        this.game = gameData;
    }

    public GameData getGameData() { return game; }
}
