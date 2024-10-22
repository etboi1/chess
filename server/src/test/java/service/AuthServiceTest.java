package service;

import dataaccess.*;
import model.AuthData;
import org.junit.jupiter.api.*;

public class AuthServiceTest {
    static private AuthDAO authDataAccess;
    static private AuthService authService;
    static private ClearService clearService;

    @BeforeAll
    public static void init() {
        UserDAO userDataAccess = new MemoryUserDAO();
        authDataAccess = new MemoryAuthDAO();
        GameDAO gameDataAccess = new MemoryGameDAO();
        authService = new AuthService(authDataAccess);
        clearService = new ClearService(userDataAccess, authDataAccess, gameDataAccess);
    }

    @BeforeEach
    public void clearState() throws DataAccessException {
        clearService.clearData();
    }

    @Test
    @DisplayName("Successful Authentication")
    public void authSuccess() throws Exception {
        String username = "realUser";
        String authToken = "goodAuth";
        authDataAccess.createAuth(new AuthData(authToken, username));
        AuthData expectedAuth = authDataAccess.getAuth(authToken);

        AuthData actualAuth = authService.authenticate(authToken);
        Assertions.assertEquals(expectedAuth, actualAuth);
    }

    @Test
    @DisplayName("Authentication Failure - user has incorrect authToken")
    public void authFailure(){
        String username = "realUser";
        String authToken = "goodAuth";
        authDataAccess.createAuth(new AuthData(authToken, username));

        Exception ex = Assertions.assertThrows(UnauthorizedException.class, () -> authService.authenticate("badAuth"));
        Assertions.assertEquals("Error: unauthorized", ex.getMessage());
    }
}
