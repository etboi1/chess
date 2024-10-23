package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import request.CreateGameRequest;
import request.JoinGameRequest;
import response.CreateGameResponse;
import response.ListGamesResponse;

import java.util.ArrayList;

public class GameServiceTests {
    static private AuthDAO authDataAccess;
    static private GameDAO gameDataAccess;
    static private GameService gameService;
    static private ClearService clearService;

    @BeforeAll
    public static void init() {
        UserDAO userDataAccess = new MemoryUserDAO();
        authDataAccess = new MemoryAuthDAO();
        gameDataAccess = new MemoryGameDAO();
        gameService = new GameService(gameDataAccess, authDataAccess);
        clearService = new ClearService(userDataAccess, authDataAccess, gameDataAccess);
    }

    @BeforeEach
    public void clearState() throws Exception {
        clearService.clearData();
    }

    @Test
    @DisplayName("Successfully List Games")
    public void listSuccess() throws Exception {
        // Add some games to the database
        ChessGame game = new ChessGame();
        GameData game1 = new GameData(1000, "white", "black", "00", game);
        GameData game2 = new GameData(1001, "white", "black", "01", game);
        gameDataAccess.createGame(game1);
        gameDataAccess.createGame(game2);

        // Call list games on server
        authDataAccess.createAuth(new AuthData("authToken", "username"));
        ListGamesResponse actualResponse = gameService.listGames("authToken");

        // Create expected response object & compare
        ArrayList<GameData> expectedGames = new ArrayList<>();
        expectedGames.add(game1);
        expectedGames.add(game2);
        ListGamesResponse expectedResponse = new ListGamesResponse(expectedGames);

        Assertions.assertEquals(expectedResponse, actualResponse);
        Assertions.assertEquals(expectedResponse.games(), actualResponse.games());
    }

    @Test
    @DisplayName("List Games Failure - Unauthenticated User")
    public void listFailure() {
        // Add some games to the database
        ChessGame game = new ChessGame();
        GameData game1 = new GameData(1000, "white", "black", "00", game);
        GameData game2 = new GameData(1001, "white", "black", "01", game);
        gameDataAccess.createGame(game1);
        gameDataAccess.createGame(game2);

        // Call list games on server, but with an authToken that doesn't match
        authDataAccess.createAuth(new AuthData("authToken", "username"));
        Exception ex = Assertions.assertThrows(UnauthorizedException.class, () -> gameService.listGames("iDon'tMatch"));
        Assertions.assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    @DisplayName("Successfully Create Game")
    public void createSuccess() throws Exception {
        authDataAccess.createAuth(new AuthData("authToken", "username"));

        CreateGameRequest createRequest = new CreateGameRequest("gameName");
        var res = gameService.createGame(createRequest, "authToken");

        Assertions.assertEquals(new CreateGameResponse(1000), res);
        Assertions.assertNotNull(gameDataAccess.getGame(res.gameID()));
        Assertions.assertEquals(new GameData(1000, null, null, "gameName", new ChessGame()),
                gameDataAccess.getGame(res.gameID()));

        // Test Making another game to make sure that gameID is counting up correctly
        createRequest = new CreateGameRequest("newName");
        var newRes = gameService.createGame(createRequest, "authToken");

        Assertions.assertEquals(new CreateGameResponse(1001), newRes);
        Assertions.assertNotNull(gameDataAccess.getGame(res.gameID()));
        Assertions.assertEquals(new GameData(1001, null, null, "newName", new ChessGame()),
                gameDataAccess.getGame(newRes.gameID()));
    }

    @Test
    @DisplayName("Create Game Failure - unauthorized user attempts to create game")
    public void createFailure() {
        authDataAccess.createAuth(new AuthData("authToken", "username"));

        // Attempt to create a game, but with an auth token that doesn't match
        CreateGameRequest createRequest = new CreateGameRequest("gameName");
        Exception ex = Assertions.assertThrows(UnauthorizedException.class, () -> gameService.createGame(createRequest, "badAuth"));
        Assertions.assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    @DisplayName("Successfully Join a Game")
    public void joinSuccess() throws Exception {
        // Manually create an authToken to be used in testing
        authDataAccess.createAuth(new AuthData("authToken", "testUser"));
        ChessGame game = new ChessGame();

        // Test Joining as white player
        gameDataAccess.createGame(new GameData(1000, null, "black", "name", game));

        gameService.joinGame(new JoinGameRequest("WHITE", 1000), "authToken");
        var updatedGame = gameDataAccess.getGame(1000);

        Assertions.assertEquals(new GameData(1000, "testUser", "black", "name", game),
                updatedGame);

        // Test joining as black player
        gameDataAccess.createGame(new GameData(1001, null, null, "name", game));

        gameService.joinGame(new JoinGameRequest("BLACK", 1001), "authToken");
        var updatedGame2 = gameDataAccess.getGame(1001);

        Assertions.assertEquals(new GameData(1001, null, "testUser", "name", game),
                updatedGame2);
    }

    @Test
    @DisplayName("Attempt to join a full game")
    public void joinFailure() {
        authDataAccess.createAuth(new AuthData("authToken", "username"));

        ChessGame game = new ChessGame();
        gameDataAccess.createGame(new GameData(1000, "white", "black", "name", game));

        Exception ex = Assertions.assertThrows(RedundantDataException.class, () -> gameService.joinGame(new JoinGameRequest("WHITE", 1000), "authToken"));
        Assertions.assertEquals("Error: already taken", ex.getMessage());
    }
}
