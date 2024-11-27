package websocket.messages;

import model.GameData;

public class LoadGameMessage extends ServerMessage {

    public final GameData gameData;

    public LoadGameMessage(ServerMessageType type, GameData gameData) {
        super(type);
        this.gameData = gameData;
    }

    public GameData getGameData() { return gameData; }
}
