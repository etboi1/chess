package ui;

import chess.*;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import response.CreateGameResponse;
import response.ListGamesResponse;
import response.LoginRegisterResponse;
import server.ServerFacade;
import websocket.WebSocketFacade;

import java.util.*;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.SET_TEXT_COLOR_GREEN;

public class ChessClient {
    private final ServerFacade server;
    private AuthData currentAuth;
    private final String serverUrl;
    private final NotificationHandler notificationHandler;
    private Integer currentGameID;
    private WebSocketFacade ws;
    public Map<Integer, Integer> gameNumToID = new HashMap<>();
    public State state = State.LOGGED_OUT;
    public PlayerColor rootColor = PlayerColor.NONE;
    public ChessGame currentGame = null;

    public ChessClient(String serverUrl, NotificationHandler notificationHandler) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
    }

    public enum PlayerColor {
        WHITE,
        BLACK,
        NONE
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
                case "redraw" -> redrawGame();
                case "highlight" -> highlightMoves(params);
                case "move" -> makeMove(params);
                case "leave" -> leaveGame();
                case "resign" -> resignGame();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            throw new Throwable(ex.getMessage());
        }
    }

    public String login(String... params) throws ResponseException {
        assertLoggedOut("login");
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
        assertLoggedOut("register");
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
            currentGameID = gameID;
            assertValidColor(params[1]);
            String playerColor = params[1].toUpperCase();
            try {
                server.joinGame(currentAuth.authToken(), playerColor, gameID);
                ws = new WebSocketFacade(serverUrl, notificationHandler, currentAuth.username());
                System.out.println();
                ws.join(currentAuth.authToken(), gameID);
                state = State.GAMEPLAY;
            } catch (ResponseException ex) {
                throw new ResponseException(400, ex.getMessage());
            }
        } else {
            throw new ResponseException(400,
                    "Expected: <GAME_NUMBER> [WHITE|BLACK], where <GAME_NUMBER> is a number matching desired game.\n");
        }
        return "";
    }

    public String observeGame(String... params) throws ResponseException {
        assertLoggedIn("observe a game");
        if (params.length == 1) {
            Integer gameID = convertGameNumToGameID(params[0]);
            currentGameID = gameID;
            ws = new WebSocketFacade(serverUrl, notificationHandler, currentAuth.username());
            ws.join(currentAuth.authToken(), gameID);
            state = State.GAMEPLAY;
        } else {
            throw new ResponseException(400, "Expected: <GAME_NUMBER>, which is a number matching desired game.\n");
        }
        return "";
    }

    public String redrawGame() throws ResponseException {
        assertInGame("redraw the chess board");
        BoardDisplay.printBoard(currentGame, rootColor == PlayerColor.BLACK);
        return "";
    }

    public String highlightMoves(String... params) throws ResponseException {
        assertInGame("highlight legal moves");
        if (params.length == 2) {
            if (currentGame.getGameState() == ChessGame.GameState.FINISHED) {
                throw new ResponseException(400, "There are no possible moves because the game is over.\n");
            }
            assertParamsNumeric(params[1]);
            int row = Integer.parseInt(params[1]);
            Integer col = convertColumn(params[0]);
            BoardDisplay.printBoard(currentGame, rootColor == PlayerColor.BLACK, new ChessPosition(row, col));
        } else {
            throw new ResponseException(400, "Expected: <COLUMN> <ROW>, which are the coordinates of " +
                    "the piece whose possible moves you'd like to see.\n");
        }
        return "";
    }

    public String makeMove(String... params) throws ResponseException {
        assertInGame("make a move");
        if (params.length == 4 | params.length == 5) {
            assertParamsNumeric(params[1], params[3]);
            Integer startCol = convertColumn(params[0]);
            Integer endCol = convertColumn(params[2]);
            ChessPosition startPosition = new ChessPosition(Integer.parseInt(params[1]), startCol);
            ChessPosition endPosition = new ChessPosition(Integer.parseInt(params[3]), endCol);
            ChessPiece.PieceType promotionType = getPromotionType(params);
            ChessMove move = new ChessMove(startPosition, endPosition, promotionType);
            ws.makeMove(currentAuth.authToken(), currentGameID, move);
        } else {
            throw new ResponseException(400, "Expected: <START_COLUMN> <START_ROW> <END_COLUMN> <END_ROW> <PAWN_PROMOTION_PIECE>\n");
        }
        return "";
    }

    private ChessPiece.PieceType getPromotionType(String[] params) throws ResponseException {
        ChessPiece.PieceType promotionType = null;
        if (params.length == 5) {
            var pieceType = params[4].toLowerCase();
            switch (pieceType) {
                case "bishop" -> promotionType = ChessPiece.PieceType.BISHOP;
                case "knight" -> promotionType = ChessPiece.PieceType.KNIGHT;
                case "queen" -> promotionType = ChessPiece.PieceType.QUEEN;
                case "rook" -> promotionType = ChessPiece.PieceType.ROOK;
                default -> throw new ResponseException(400,
                        "Pawn promotion piece must be bishop, knight, queen, or rook.\n");
            }
        }
        return promotionType;
    }

    public String leaveGame() throws ResponseException {
        assertInGame("leave a game");
        ws.leave(currentAuth.authToken(), currentGameID);
        rootColor = PlayerColor.NONE;
        state = State.LOGGED_IN;
        return "You have successfully left the game.\n";
    }

    public String resignGame() throws ResponseException {
        if (currentGame.getGameState() == ChessGame.GameState.FINISHED) {
            throw new ResponseException(400, "You cannot resign because the game is already over.");
        }
        Scanner scanner = new Scanner(System.in);
        System.out.println("Are you sure you want to resign? (Y|N)\n" +
                "\n" + RESET_TEXT_BLINKING + RESET_TEXT_COLOR +
                SET_TEXT_BLINKING + SET_TEXT_COLOR_GREEN +
                "[" + state + "] " + ">>> ");
        String input = scanner.nextLine();
        if (input.equalsIgnoreCase("Y")) {
            assertInGame("resign a game");
            ws.resign(currentAuth.authToken(), currentGameID);
            return "You have successfully resigned.\n";
        }
        return "";
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
        } else if (state == State.LOGGED_IN) {
            return """
                Possible Commands:
                \tCOMMAND <PARAMETERS> - DESCRIPTION OF COMMAND
                
                \tcreate <NAME> - a game
                \tlist - games
                \tjoin <GAME_NUMBER> [WHITE|BLACK] - a game
                \tobserve <GAME_NUMBER> - a game
                \tlogout - when you are done
                \thelp - with possible commands
                """;
        }
        return """
                Possible Commands:
                \tCOMMAND <PARAMETERS> - DESCRIPTION OF COMMAND
                
                \tredraw - the chess board
                \thighlight <COLUMN> <ROW> - a piece's all legal moves
                \tmove <START_COLUMN> <START_ROW> <END_COLUMN> <END_ROW> <PAWN_PROMOTION_PIECE> - make a move \
                (and provide promotion piece type if moving pawn to end of board)
                \tleave - quit this game
                \tresign - forfeit game
                \thelp - with possible commands
                """;
    }

    private void assertLoggedOut(String attemptedFunction) throws ResponseException {
        if (state != State.LOGGED_OUT) {
            throw new ResponseException(400, "You must be logged out to " + attemptedFunction + ".\n");
        }
    }

    private void assertLoggedIn(String attemptedFunction) throws ResponseException {
        if (state != State.LOGGED_IN) {
            throw new ResponseException(400, "You must be logged in and not in a game to " + attemptedFunction + ".\n");
        }
    }

    private void assertInGame(String attemptedFunction) throws ResponseException {
        if (state != State.GAMEPLAY) {
            throw new ResponseException(400, "You must join a game to " + attemptedFunction + ".\n");
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

    private void assertParamsNumeric(String... params) throws ResponseException {
        for (String param : params) {
            try {
                int coordinate = Integer.parseInt(param);
                if (coordinate < 1 | coordinate > 8) {
                    throw new ResponseException(400, "Row values must be between 1-8.\n");
                }
            } catch (NumberFormatException ex) {
                throw new ResponseException(400, "Row values must be numbers.\n");
            }
        }
    }

    private Integer convertColumn(String col) throws ResponseException {
        final Map<String, Integer> conversionMap = Map.of(
                "a", 1,
                "b", 2,
                "c", 3,
                "d", 4,
                "e", 5,
                "f", 6,
                "g", 7,
                "h", 8
        );
        if (!conversionMap.containsKey(col)) {
            throw new ResponseException(400, "Column values must be letters between a-h.\n");
        }
        return conversionMap.get(col);
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
}
