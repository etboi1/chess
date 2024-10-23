package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClearServiceTest {
    static private UserDAO userDataAccess;
    static private AuthDAO authDataAccess;
    static private GameDAO gameDataAccess;
    static private ClearService clearService;
    static private UserService userService;

    @BeforeAll
    public static void init() {
        userDataAccess = new MemoryUserDAO();
        authDataAccess = new MemoryAuthDAO();
        gameDataAccess = new MemoryGameDAO();
        clearService = new ClearService(userDataAccess, authDataAccess, gameDataAccess);
        userService = new UserService(authDataAccess, userDataAccess);
    }

    @Test
    public void clearData() throws Exception {
        //Add a user, authData, and game to each hashmap stored in memory
        UserData user = new UserData("testUsername", "testPassword", "testEmail");
        AuthData auth = new AuthData("authToken", "testUser");
        GameData game = new GameData(1234, "black", "white", "name", new ChessGame());
        userDataAccess.createUser(user);
        authDataAccess.createAuth(auth);
        gameDataAccess.createGame(game);

        //Now clear and make sure they're empty
        clearService.clearData();

        Assertions.assertNull(userDataAccess.getUser("testUsername"));
        Assertions.assertNull(authDataAccess.getAuth(auth.authToken()));
        Assertions.assertTrue(gameDataAccess.listGames().isEmpty());
    }
}
