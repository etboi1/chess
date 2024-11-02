package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import org.junit.jupiter.api.*;
import service.ClearService;
import service.UserService;

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
    String statement = "INSERT INTO games (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";

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
}
