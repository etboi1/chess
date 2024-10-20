package service;

import chess.ChessGame;
import dataaccess.*;
import model.GameData;
import org.junit.jupiter.api.*;
import request.CreateGameRequest;
import request.JoinGameRequest;
import response.CreateGameResponse;
import response.ListGamesResponse;

import java.util.ArrayList;

public class GameServiceTests {
    static private UserDAO userDataAccess;
    static private AuthDAO authDataAccess;
    static private GameDAO gameDataAccess;
    static private GameService gameService;
    static private ClearService clearService;

    @BeforeAll
    public static void init() {
        userDataAccess = new MemoryUserDAO();
        authDataAccess = new MemoryAuthDAO();
        gameDataAccess = new MemoryGameDAO();
        gameService = new GameService(gameDataAccess);
        clearService = new ClearService(userDataAccess, authDataAccess, gameDataAccess);
    }

    @BeforeEach
    public void clearState() throws Exception {
        clearService.clearData();
    }

    @Test
    @DisplayName("Successfully List Games")
    public void listSuccess() {
        // Add some games to the database
        ChessGame game = new ChessGame();
        GameData game1 = new GameData(1000, "white", "black", "00", game);
        GameData game2 = new GameData(1001, "white", "black", "01", game);
        gameDataAccess.createGame(game1);
        gameDataAccess.createGame(game2);

        // Call list games on server
        ListGamesResponse actualResponse = gameService.listGames();

        // Create expected response object & compare
        ArrayList<GameData> expectedGames = new ArrayList<>();
        expectedGames.add(game1);
        expectedGames.add(game2);
        ListGamesResponse expectedResponse = new ListGamesResponse(expectedGames);

        Assertions.assertEquals(expectedResponse, actualResponse);
        Assertions.assertEquals(expectedResponse.games(), actualResponse.games());
    }

    @Test
    @DisplayName("Successfully Create Game")
    public void createSuccess() throws Exception{
        CreateGameRequest createRequest = new CreateGameRequest("gameName");
        var res = gameService.createGame(createRequest);

        Assertions.assertEquals(new CreateGameResponse(1000), res);
        Assertions.assertNotNull(gameDataAccess.getGame(res.gameID()));
        Assertions.assertEquals(new GameData(1000, null, null, "gameName", new ChessGame()),
                gameDataAccess.getGame(res.gameID()));

        createRequest = new CreateGameRequest("newName");
        var newRes = gameService.createGame(createRequest);

        Assertions.assertEquals(new CreateGameResponse(1001), newRes);
        Assertions.assertNotNull(gameDataAccess.getGame(res.gameID()));
        Assertions.assertEquals(new GameData(1001, null, null, "newName", new ChessGame()),
                gameDataAccess.getGame(newRes.gameID()));
    }

    @Test
    @DisplayName("Successfully Join a Game")
    public void joinSuccess() throws Exception {
        ChessGame game = new ChessGame();
        gameDataAccess.createGame(new GameData(1000, null, "black", "name", game));

        gameService.joinGame(new JoinGameRequest("white", 1000, "testUser"));
        var updatedGame = gameDataAccess.getGame(1000);

        Assertions.assertEquals(new GameData(1000, "testUser", "black", "name", game),
                updatedGame);
    }

    @Test
    @DisplayName("Attempt to join a full game")
    public void joinFailure() throws Exception {
        ChessGame game = new ChessGame();
        gameDataAccess.createGame(new GameData(1000, "white", "black", "name", game));

        Exception ex = Assertions.assertThrows(RedundantDataException.class, () -> {
            gameService.joinGame(new JoinGameRequest("white", 1000, "testUser"));
        });
        Assertions.assertEquals("Error: Already taken", ex.getMessage());
    }
}
