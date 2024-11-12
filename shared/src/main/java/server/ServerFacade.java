package server;

import request.JoinGameRequest;
import response.CreateGameResponse;
import response.ListGamesResponse;
import response.LoginRegisterResponse;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {serverUrl = url;}

    public LoginRegisterResponse register(String username, String password, String email) {
        return null;
    }

    public LoginRegisterResponse login(String username, String password) {
        return null;
    }

    public void logout(String authToken) {

    }

    public CreateGameResponse createGame(String authToken, String gameName) {
        return null;
    }

    public ListGamesResponse listGames(String authToken) {
        return null;
    }

    public void joinGame(String authToken, String playerColor, String gameID) {

    }
}
