package dataaccess;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import model.GameData;
import org.junit.jupiter.api.*;
import service.ClearService;
import service.UserService;
import spark.utils.Assert;

import java.util.ArrayList;

public class GameDataAccessTests {
    static private BaseMySqlDAO baseDataAccess;
    static private GameDAO gameDataAccess;
    static private ClearService clearService;

    @BeforeAll
    public static void init() throws DataAccessException {
        baseDataAccess = new BaseMySqlDAO();
        UserDAO userDataAccess = new MySqlUserDAO();
        AuthDAO authDataAccess = new MySqlAuthDAO();
        gameDataAccess = new MySqlGameDAO();
        clearService = new ClearService(userDataAccess, authDataAccess, gameDataAccess);
    }

    GameData goodGame = new GameData(null, "white", null, "name", new ChessGame());

    @BeforeEach
    public void clearState() throws Exception {
        clearService.clearData();
    }

    @Test
    @DisplayName("Successfully Create Game")
    public void createGameSuccess() throws Exception {
        gameDataAccess.createGame(goodGame);
        var statement = "SELECT * FROM games WHERE gameID=?";
        var rs = baseDataAccess.performQuery(statement, 1);
        Assertions.assertTrue(rs.next());
        Assertions.assertEquals(goodGame.blackUsername(), rs.getString("blackUsername"));
        Assertions.assertEquals(goodGame.whiteUsername(), rs.getString("whiteUsername"));
        Assertions.assertEquals(goodGame.gameName(), rs.getString("gameName"));
        var json = rs.getString("game");
        ChessGame storedGame = new Gson().fromJson(json, ChessGame.class);
        Assertions.assertEquals(goodGame.game(), storedGame);
        System.out.println(storedGame);
    }

    @Test
    @DisplayName("Create Game Failure - game is null")
    public void createGameFailure() {
        GameData badGame = new GameData(null, "black", null, "gameName", null);
        Assertions.assertThrows(DataAccessException.class, () -> gameDataAccess.createGame(badGame));
    }

    @Test
    @DisplayName("Get Game Success")
    public void getGameSuccess() throws Exception {
        var statement = "INSERT INTO games (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";
        baseDataAccess.performUpdate(statement, goodGame.whiteUsername(), goodGame.blackUsername(),
                goodGame.gameName(), new Gson().toJson(goodGame.game()));
        GameData storedGame = gameDataAccess.getGame(1);
        Assertions.assertNotNull(storedGame);
        Assertions.assertEquals(goodGame.game(), storedGame.game());
        Assertions.assertEquals(goodGame.whiteUsername(), storedGame.whiteUsername());
    }

    @Test
    @DisplayName("Fail to Get Game - no gameID supplied")
    public void getGameFailure() throws Exception{
        var statement = "INSERT INTO games (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";
        baseDataAccess.performUpdate(statement, goodGame.whiteUsername(), goodGame.blackUsername(),
                goodGame.gameName(), new Gson().toJson(goodGame.game()));
        var storedGame = gameDataAccess.getGame(null);
        Assertions.assertNull(storedGame);
    }

    @Test
    @DisplayName("List Games Successfully")
    public void listGamesSuccess() throws Exception {
        Assertions.assertTrue(gameDataAccess.listGames().isEmpty());

        gameDataAccess.createGame(goodGame);
        GameData game2 = new GameData(null, "white", "black", "bestGame", new ChessGame());
        gameDataAccess.createGame(game2);

        ArrayList<GameData> storedList = gameDataAccess.listGames();
        Assertions.assertNotNull(storedList);
        Assertions.assertEquals(goodGame.gameName(), storedList.getFirst().gameName());
        Assertions.assertEquals(game2.gameName(), storedList.get(1).gameName());

        gameDataAccess.clear();
        Assertions.assertTrue(gameDataAccess.listGames().isEmpty());
    }

    @Test
    @DisplayName("List Games Fringe Case - No Games Exist")
    public void listGamesEmpty() throws Exception {
        ArrayList<GameData> storedList = gameDataAccess.listGames();
        Assertions.assertTrue(storedList.isEmpty());
    }

    @Test
    @DisplayName("Update Game Successfully - add new user")
    public void updateGame() throws Exception {
        gameDataAccess.createGame(goodGame);
        GameData updatedGame = new GameData(1, goodGame.whiteUsername(), "black", goodGame.gameName(), goodGame.game());
        gameDataAccess.updateGame(1, updatedGame);
        GameData storedGame = gameDataAccess.getGame(1);
        Assertions.assertEquals(updatedGame, storedGame);
        Assertions.assertNotEquals(goodGame, storedGame);
    }

    @Test
    @DisplayName("Update Game Failure - invalid gameName")
    public void updateGameFailure() throws Exception {
        gameDataAccess.createGame(goodGame);
        GameData updatedGame = new GameData(1, goodGame.whiteUsername(), "black", null, goodGame.game());
        Assertions.assertThrows(DataAccessException.class, () -> gameDataAccess.updateGame(updatedGame.gameID(), updatedGame));
    }
}
