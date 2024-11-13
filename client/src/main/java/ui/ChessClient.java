package ui;

import exception.ResponseException;
import model.AuthData;
import model.GameData;
import response.CreateGameResponse;
import response.ListGamesResponse;
import response.LoginRegisterResponse;
import server.ServerFacade;

import java.util.ArrayList;
import java.util.Arrays;

public class ChessClient {
    private final ServerFacade server;
    private AuthData currentAuth;
    private State state = State.LOGGEDOUT;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "login" -> login(params);
                case "register" -> register(params);
                case "logout" -> logout();
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> joinGame(params);
                case "observe" -> observeGame(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String login(String... params) throws ResponseException {
        if (params.length == 2) {
            String username = params[0];
            String password = params[1];
            try {
                LoginRegisterResponse loginRes = server.login(username, password);
                currentAuth = new AuthData(loginRes.authToken(), loginRes.username());
                state = State.LOGGEDIN;
                return String.format("You signed in as %s.\n", currentAuth.username());
            } catch (ResponseException ex) {
                throw new ResponseException(400, ex.getMessage());
            }
        }
        throw new ResponseException(400, "Expected: <USERNAME> <PASSWORD>\n");
    }

    public String register(String... params) throws ResponseException {
        if (params.length ==3) {
            String username = params[0];
            String password = params[1];
            String email = params[2];
            try {
                LoginRegisterResponse registerRes = server.register(username, password, email);
                currentAuth = new AuthData(registerRes.authToken(), registerRes.username());
                state = State.LOGGEDIN;
                return String.format("You registered and signed in as %s.\n", currentAuth.username());
            } catch (ResponseException ex) {
                throw new ResponseException(400, ex.getMessage());
            }
        }
        throw new ResponseException(400, "Expected: <USERNAME> <PASSWORD> <EMAIL>\n");
    }

    public String logout() throws ResponseException {
        assertLoggedIn("logout");
        try {
            server.logout(currentAuth.authToken());
            state = State.LOGGEDOUT;
            currentAuth = null;
        } catch (ResponseException ex) {
            throw new ResponseException(400, ex.getMessage());
        }
        return "You have been logged out.\n";
    }

    public String createGame(String... params) throws ResponseException {
        assertLoggedIn("create a game");
        if (params.length == 1) {
            String gameName = params[0];
            try {
                CreateGameResponse createRes = server.createGame(currentAuth.authToken(), gameName);
                return String.format("Created game with ID %s and name %s.\n", createRes.gameID(), gameName);
            } catch (ResponseException ex) {
                throw new ResponseException(400, ex.getMessage());
            }
        }
        throw new ResponseException(400, "Expected: <NAME> (only one name accepted at a time).\n");
    }

    public String listGames() throws ResponseException {
        assertLoggedIn("list games");
        try {
            ListGamesResponse listRes = server.listGames(currentAuth.authToken());
            ArrayList<GameData> games = listRes.games();
            String display = "";
            for (GameData game : games) {
                String black = "AVAILABLE";
                String white = "AVAILABLE";
                if (game.blackUsername() != null) {
                    black = game.blackUsername();
                }
                if (game.whiteUsername() != null) {
                    white = game.whiteUsername();
                }
                display += String.format("[%s] GameName: %s, White Player: %s, BlackPlayer: %s\n",
                        game.gameID(), game.gameName(), white, black);
            }
            return display;
        } catch (ResponseException ex) {
            throw new ResponseException(400, ex.getMessage());
        }
    }

    public String joinGame(String... params) throws ResponseException {
        assertLoggedIn("join a game");
        return null;
    }

    public String observeGame(String... params) throws ResponseException {
        assertLoggedIn("observe a game");
        return null;
    }

    public String help() {
        if (state == State.LOGGEDOUT) {
            return """
                    register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                    login <USERNAME> <PASSWORD> - to play chess
                    quit - playing chess
                    help - with possible commands
                    """;
        }
        return """
                create <NAME> - a game
                list - games
                join <ID> [WHITE|BLACK] - a game
                observe <ID> - a game
                logout - when you are done
                quit - playing chess
                help - with possible commands
                """;
    }

    private void assertLoggedIn(String attemptedFunction) throws ResponseException {
        if (state == State.LOGGEDOUT) {
            throw new ResponseException(400, "You must login to " + attemptedFunction + ".\n");
        }
    }
}
