package service;

import chess.ChessGame;
import dataaccess.GameDAO;
import model.GameData;
import request.CreateGameRequest;
import request.JoinGameRequest;
import response.CreateGameResponse;
import response.ListGamesResponse;

import java.util.ArrayList;

public class GameService {
    private final GameDAO gameDataAccess;

    public GameService(GameDAO gameDataAccess) {
        this.gameDataAccess = gameDataAccess;
    }

    public ListGamesResponse listGames() {
        return new ListGamesResponse(gameDataAccess.listGames());
    }

    public CreateGameResponse createGame(CreateGameRequest createRequest) {
        ArrayList<GameData> allGames = gameDataAccess.listGames();
        int idNum = allGames.size();
        int gameID = 1000 + idNum;

        GameData newGame = new GameData(gameID, null, null, createRequest.gameName(), new ChessGame());
        gameDataAccess.createGame(newGame);
        return new CreateGameResponse(gameID);
    }

    public void joinGame(JoinGameRequest joinRequest) {
    }
}
