package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LogoutTest {
    static private UserDAO userDataAccess;
    static private AuthDAO authDataAccess;
    static private UserService userService;

    @BeforeAll
    public static void init() {
        userDataAccess = new MemoryUserDAO();
        authDataAccess = new MemoryAuthDAO();
        userService = new UserService(authDataAccess, userDataAccess);
    }

    @Test
    @DisplayName("Successful logout")
    public void logoutSuccess() throws Exception {
        UserData user = new UserData("bill", "billisbest", "bill@bill.com");
        AuthData auth = new AuthData("authToken", "bill");
        userDataAccess.createUser(user);
        authDataAccess.createAuth(auth);

        userService.logoutUser(auth);

        Assertions.assertNull(authDataAccess.getAuth(auth));
    }

    @Test
    @DisplayName("Unsuccessful logout - wrong authtoken")
    public void logoutFailure() throws Exception {
        UserData user = new UserData("bill", "billisbest", "bill@bill.com");
        AuthData auth = new AuthData("authToken", "bill");
        AuthData badAuth = new AuthData("badAuth", "bill");
        userDataAccess.createUser(user);
        authDataAccess.createAuth(auth);

        Exception ex = Assertions.assertThrows(UnauthorizedException.class, () -> {
            userService.logoutUser(badAuth);
        });
        Assertions.assertEquals("Error: unauthorized", ex.getMessage());
    }
}
