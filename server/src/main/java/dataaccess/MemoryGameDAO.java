package dataaccess;

import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {
    private final HashMap<Integer, GameData> games = new HashMap<>();

    @Override
    public void clear() {
        games.clear();
    }

    @Override
    public int createGame(GameData newGame) {
        ArrayList<GameData> allGames = listGames();
        int idNum = allGames.size();
        int gameID = idNum + 1;
        GameData game = new GameData(gameID, newGame.whiteUsername(), newGame.blackUsername(), newGame.gameName(), newGame.game());
        games.put(newGame.gameID(), game);
        return gameID;
    }

    @Override
    public GameData getGame(Integer gameID) throws Exception {
        return games.get(gameID);
    }

    @Override
    public ArrayList<GameData> listGames() {
        ArrayList<GameData> allGames = new ArrayList<>();
        for (HashMap.Entry<Integer, GameData> game : games.entrySet()) {
            allGames.add(game.getValue());
        }
        return allGames;
    }

    @Override
    public void updateGame(Integer gameID, GameData gameUpdate) {
        games.remove(gameID);
        games.put(gameID, gameUpdate);
    }
}
