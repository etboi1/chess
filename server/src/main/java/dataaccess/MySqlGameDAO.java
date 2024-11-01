package dataaccess;

import model.GameData;

import java.util.ArrayList;

public class MySqlGameDAO extends BaseMySqlDAO implements GameDAO {
    public MySqlGameDAO() throws DataAccessException {
        super();
    }

    @Override
    public void clear() {

    }

    @Override
    public void createGame(GameData newGame) {

    }

    @Override
    public GameData getGame(Integer gameID) throws Exception {
        return null;
    }

    @Override
    public ArrayList<GameData> listGames() {
        return null;
    }

    @Override
    public void updateGame(Integer gameID, GameData gameUpdate) {

    }
}
