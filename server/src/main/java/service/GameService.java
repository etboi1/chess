package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.GameData;
import request.CreateGameRequest;
import request.JoinGameRequest;
import response.CreateGameResponse;
import response.ListGamesResponse;

import java.util.ArrayList;
import java.util.Objects;

public class GameService extends AuthService {
    private final GameDAO gameDataAccess;

    public GameService(GameDAO gameDataAccess, AuthDAO authDataAccess) {
        super(authDataAccess);
        this.gameDataAccess = gameDataAccess;
    }

    public ListGamesResponse listGames(String authToken) throws Exception {
        super.authenticate(authToken);

        return new ListGamesResponse(gameDataAccess.listGames());
    }

    public CreateGameResponse createGame(CreateGameRequest createRequest, String authToken) throws Exception {
        super.authenticate(authToken);

        GameData newGame = new GameData(null, null, null, createRequest.gameName(), new ChessGame());
        var gameID = gameDataAccess.createGame(newGame);
        return new CreateGameResponse(gameID);
    }

    public void joinGame(JoinGameRequest joinRequest, String authToken) throws Exception {
        String currentUser = super.authenticate(authToken).username();
        GameData currentGame = gameDataAccess.getGame(joinRequest.gameID());
        if (currentGame == null) {
            throw new BadRequestException("Error: bad request");
        }
        GameData updatedGame;

        if (!Objects.equals(joinRequest.playerColor(), "WHITE") && !Objects.equals(joinRequest.playerColor(), "BLACK")) {
            throw new BadRequestException("Error: bad request");
        }

        if (Objects.equals(joinRequest.playerColor(), "WHITE")) {
            if (currentGame.whiteUsername() != null) {
                throw new RedundantDataException("Error: already taken");
            }
            updatedGame = new GameData(joinRequest.gameID(), currentUser, currentGame.blackUsername(), currentGame.gameName(), currentGame.game());
        } else {
            if (currentGame.blackUsername() != null) {
                throw new RedundantDataException("Error: already taken");
            }
            updatedGame = new GameData(joinRequest.gameID(), currentGame.whiteUsername(), currentUser, currentGame.gameName(), currentGame.game());
        }
        gameDataAccess.updateGame(joinRequest.gameID(), updatedGame);
    }
}
