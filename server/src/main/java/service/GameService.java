package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;

public class GameService {
    private final GameDAO gameDataAccess;

    public GameService(GameDAO gameDataAccess) {
        this.gameDataAccess = gameDataAccess;
    }
}
