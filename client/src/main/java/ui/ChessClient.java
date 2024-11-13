package ui;

import exception.ResponseException;
import server.ServerFacade;

import java.util.Arrays;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.LOGGEDOUT;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split("");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "login" -> login();
                case "register" -> register();
                case "logout" -> logout();
                case "create" -> createGame();
                case "list" -> listGames();
                case "join" -> joinGame();
                case "observe" -> observeGame();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String login() throws ResponseException {
        return null;
    }

    public String register() {
        return null;
    }

    public String logout() {
        return null;
    }

    public String createGame() {
        return null;
    }

    public String listGames() {
        return null;
    }

    public String joinGame() {
        return null;
    }

    public String observeGame() {
        return null;
    }

    public String help() {
        return null;
    }
}
