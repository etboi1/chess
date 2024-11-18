package ui;

import chess.ChessBoard;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import response.CreateGameResponse;
import response.ListGamesResponse;
import response.LoginRegisterResponse;
import server.ServerFacade;

import java.util.*;

import static ui.BoardDisplay.*;

public class ChessClient {
    private final ServerFacade server;
    private AuthData currentAuth;
    public State state = State.LOGGED_OUT;
    public Map<Integer, Integer> gameNumToID = new HashMap<>();

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public String eval(String input) throws Throwable {
        try {
            var tokens = input.split(" ");
            var cmd = (tokens.length > 0) ? tokens[0].toLowerCase() : "help";
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
            throw new Throwable(ex.getMessage());
        }
    }

    public String login(String... params) throws ResponseException {
        if (params.length == 2) {
            String username = params[0];
            String password = params[1];
            try {
                LoginRegisterResponse loginRes = server.login(username, password);
                currentAuth = new AuthData(loginRes.authToken(), loginRes.username());
                state = State.LOGGED_IN;
                return String.format("You are signed in as %s.\n", currentAuth.username());
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
                state = State.LOGGED_IN;
                return String.format("You have successfully registered and are signed in as %s!\n", currentAuth.username());
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
            state = State.LOGGED_OUT;
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
                return String.format("Created game with name %s!\n", gameName);
            } catch (ResponseException ex) {
                throw new ResponseException(400, ex.getMessage());
            }
        }
        throw new ResponseException(400, "Expected: <NAME> (exactly one name accepted at a time).\n");
    }

    public String listGames() throws ResponseException {
        assertLoggedIn("list games");
        int gameNum = 1;
        try {
            ListGamesResponse listRes = server.listGames(currentAuth.authToken());
            ArrayList<GameData> games = listRes.games();
            String display = "";
            if (games.isEmpty()) {
                display = "No games created - be the first to start a game!\n";
            }
            for (GameData game : games) {
                String black = "AVAILABLE";
                String white = "AVAILABLE";
                if (game.blackUsername() != null) {
                    black = game.blackUsername();
                }
                if (game.whiteUsername() != null) {
                    white = game.whiteUsername();
                }
                gameNumToID.put(gameNum, game.gameID());
                display += String.format("[%s] GameName: %s, WhitePlayer: %s, BlackPlayer: %s\n",
                        gameNum, game.gameName(), white, black);
                gameNum++;
            }
            return display;
        } catch (ResponseException ex) {
            throw new ResponseException(400, ex.getMessage());
        }
    }

    public String joinGame(String... params) throws ResponseException {
        assertLoggedIn("join a game");
        if (params.length == 2) {
            Integer gameID = convertGameNumToGameID(params[0]);
            assertValidColor(params[1]);
            String playerColor = params[1].toUpperCase();
            try {
                server.joinGame(currentAuth.authToken(), playerColor, gameID);
                loadBoard();
                System.out.println();
                return "Successfully joined game!\n";
            } catch (ResponseException ex) {
                throw new ResponseException(400, ex.getMessage());
            }
        }
        throw new ResponseException(400,
                "Expected: <GAME_NUMBER> [WHITE|BLACK], where <GAME_NUMBER> is a number matching desired game.\n");
    }

    public String observeGame(String... params) throws ResponseException {
        assertLoggedIn("observe a game");
        if (params.length == 1) {
            convertGameNumToGameID(params[0]);
            loadBoard();
        }
        return "\nNot yet implemented - coming phase 6!\n";
    }

    public String help() {
        if (state == State.LOGGED_OUT) {
            return """
                    Possible Commands:
                    \tCOMMAND <PARAMETERS> - DESCRIPTION OF COMMAND
                    
                    \tregister <USERNAME> <PASSWORD> <EMAIL> - to create an account
                    \tlogin <USERNAME> <PASSWORD> - to play chess
                    \tquit - playing chess
                    \thelp - with possible commands
                    """;
        }
        return """
                Possible Commands:
                \tCOMMAND <PARAMETERS> - DESCRIPTION OF COMMAND
                
                \tcreate <NAME> - a game
                \tlist - games
                \tjoin <GAME_NUMBER> [WHITE|BLACK] - a game
                \tobserve <GAME_NUMBER> - a game
                \tlogout - when you are done
                \tquit - playing chess
                \thelp - with possible commands
                """;
    }

    private void assertLoggedIn(String attemptedFunction) throws ResponseException {
        if (state == State.LOGGED_OUT) {
            throw new ResponseException(400, "You must login to " + attemptedFunction + ".\n");
        }
    }

    private void assertIsNumeric(String potentialID) throws ResponseException {
        try {
            Integer.parseInt(potentialID);
        } catch (NumberFormatException e) {
            throw new ResponseException(400, "<GAME_NUMBER> must be a number matching desired game.\n");
        }
    }

    private void assertValidColor(String colorInput) throws ResponseException {
        if (!Objects.equals(colorInput.toUpperCase(), "WHITE") && !Objects.equals(colorInput.toUpperCase(), "BLACK")) {
            throw new ResponseException(400, "Second parameter must be \"WHITE\" or \"BLACK\".\n");
        }
    }

    private Integer convertGameNumToGameID(String gameNumber) throws ResponseException {
        assertIsNumeric(gameNumber);
        Integer listNum = Integer.parseInt(gameNumber);
        Integer gameID = gameNumToID.get(listNum);
        if (gameID == null) {
            throw new ResponseException(400, """
                    Provided GAME_NUMBER does not \
                    match any numbers from previously listed games.
                    Please list games again and double check \
                    GAME_NUMBER matches desired game.
                    """);
        }
        return gameID;
    }

    private void loadBoard() {
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        BoardDisplay.displayBoard(board);
    }
}
