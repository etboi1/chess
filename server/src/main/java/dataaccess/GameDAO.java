package dataaccess;

import model.GameData;

public interface GameDAO {
    void clear();
    void createGame(String gameName);
    GameData getGame(String gameID);
    GameData[] listGames();
    void joinGame(String gameID, GameData gameUpdate);
}
