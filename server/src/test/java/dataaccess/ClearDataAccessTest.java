package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import service.ClearService;
import service.UserService;

import java.sql.*;
import java.util.ArrayList;

public class ClearDataAccessTest {
    static private UserDAO userDataAccess;
    static private AuthDAO authDataAccess;
    static private GameDAO gameDataAccess;
    static private ClearService clearService;
    static private UserService userService;

    @BeforeAll
    public static void init() throws DataAccessException {
        userDataAccess = new MySqlUserDAO();
        authDataAccess = new MySqlAuthDAO();
        gameDataAccess = new MySqlGameDAO();
        clearService = new ClearService(userDataAccess, authDataAccess, gameDataAccess);
    }

    @BeforeEach
    public void clearState() throws Exception {
        clearService.clearData();
    }

    @Test
    public void clearData() throws Exception {
        //Add data to each table
        UserData user = new UserData("username", "password", "email");
        AuthData authData = new AuthData("authToken", "username");
        GameData gameData = new GameData(null, "white", "black", "name", new ChessGame());

        userDataAccess.createUser(user);
        authDataAccess.createAuth(authData);
        gameDataAccess.createGame(gameData);

        //Make sure it was actually added
        UserData storedUser = userDataAccess.getUser(user.username());
        AuthData storedAuth = authDataAccess.getAuth(authData.authToken());
        ArrayList<GameData> storedGames = gameDataAccess.listGames();

        Assertions.assertEquals(user.username(), storedUser.username());
        Assertions.assertEquals(authData, storedAuth);
        Assertions.assertNotNull(storedGames);
        Assertions.assertFalse(storedGames.isEmpty());

        //Now clear
        userDataAccess.clear();
        authDataAccess.clear();
        gameDataAccess.clear();

        //Try and retrieve data and make sure it's null
        Assertions.assertNull(userDataAccess.getUser(user.username()));
        Assertions.assertNull(authDataAccess.getAuth(authData.authToken()));
        Assertions.assertTrue(gameDataAccess.listGames().isEmpty());
    }
}
