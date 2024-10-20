package service;

import dataaccess.*;
import model.UserData;
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
        userService = new UserService(userDataAccess, authDataAccess);
    }

    @Test
    public void clearData() throws Exception{
        UserData user = new UserData("tusername", "tpassword", "temail");
        userService.registerUser(user);
        clearService.clearData();
    }
}
