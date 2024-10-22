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

public class GameService extends AuthenticationService {
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

        ArrayList<GameData> allGames = gameDataAccess.listGames();
        int idNum = allGames.size();
        int gameID = 1000 + idNum;

        GameData newGame = new GameData(gameID, null, null, createRequest.gameName(), new ChessGame());
        gameDataAccess.createGame(newGame);
        return new CreateGameResponse(gameID);
    }

    public void joinGame(JoinGameRequest joinRequest, String authToken) throws Exception {
        String currentUser = super.authenticate(authToken).username();
        GameData currentGame = gameDataAccess.getGame(joinRequest.gameID());
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
