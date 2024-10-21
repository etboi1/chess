package dataaccess;

import model.GameData;
import service.BadRequestException;

import java.util.ArrayList;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO{
    private final HashMap<Integer, GameData> games = new HashMap<>();

    @Override
    public void clear() {
        games.clear();
    }

    @Override
    public void createGame(GameData newGame) {
        games.put(newGame.gameID(), newGame);
    }

    @Override
    public GameData getGame(Integer gameID) throws Exception {
        if (games.get(gameID) == null) {
            throw new BadRequestException("Error: bad request");
        }
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
    public void joinGame(String gameID, GameData gameUpdate) {

    }
}
