package dataaccess;

import model.GameData;

import java.util.ArrayList;

public interface GameDAO {
    void clear();
    void createGame(GameData newGame);
    GameData getGame(Integer gameID) throws Exception;
    ArrayList<GameData> listGames();
    void joinGame(String gameID, GameData gameUpdate);
}
