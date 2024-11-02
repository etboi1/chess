package dataaccess;

import model.GameData;

import java.util.ArrayList;

public interface GameDAO {
    void clear() throws DataAccessException;

    int createGame(GameData newGame) throws Exception;

    GameData getGame(Integer gameID) throws Exception;

    ArrayList<GameData> listGames() throws DataAccessException;

    void updateGame(Integer gameID, GameData gameUpdate) throws Exception;
}
