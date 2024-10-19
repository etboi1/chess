package dataaccess;

import model.GameData;

public class MemoryGameDAO implements GameDAO{
    @Override
    public void clear() {

    }

    @Override
    public void createGame(String gameName) {

    }

    @Override
    public GameData getGame(String gameID) {
        return null;
    }

    @Override
    public GameData[] listGames() {
        return new GameData[0];
    }

    @Override
    public void joinGame(String gameID, GameData gameUpdate) {

    }
}
