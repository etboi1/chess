package dataaccess;

import model.GameData;

import java.util.ArrayList;

public interface GameDAO {
    void clear() throws DataAccessException;

    void createGame(GameData newGame);

    GameData getGame(Integer gameID) throws Exception;

    ArrayList<GameData> listGames();

    void updateGame(Integer gameID, GameData gameUpdate);
}
