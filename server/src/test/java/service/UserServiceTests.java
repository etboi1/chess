package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import response.LoginRegisterResponse;

public class UserServiceTests {
    static private UserDAO userDataAccess;
    static private AuthDAO authDataAccess;
    static private GameDAO gameDataAccess;
    static private UserService userService;
    static private ClearService clearService;

    @BeforeAll
    public static void init() {
        userDataAccess = new MemoryUserDAO();
        authDataAccess = new MemoryAuthDAO();
        gameDataAccess = new MemoryGameDAO();
        userService = new UserService(authDataAccess, userDataAccess);
        clearService = new ClearService(userDataAccess, authDataAccess, gameDataAccess);
    }

    @BeforeEach
    public void clearState() throws Exception {
        clearService.clearData();
    }

    @Test
    @DisplayName("Normal User Registration")
    public void RegisterSuccess() throws Exception {
        UserData user = new UserData("testUser", "testPassword", "testEmail");
        var result = userService.registerUser(user);

        // Make sure the response object has the needed info by checking that
        // the response object isn't null, has the correct username, and has an authToken
        Assertions.assertNotNull(result);
        Assertions.assertEquals(user.username(), result.username());
        Assertions.assertNotNull(result.authToken());

        //Check also that the data is actually stored
        Assertions.assertEquals(user, userDataAccess.getUser(user.username()));
        Assertions.assertEquals(result.authToken(), authDataAccess.getAuth(new AuthData(result.authToken(), null)).authToken());
    }

    @Test
    @DisplayName("Registering an existing username")
    public void RegisterFailure() throws Exception {
        //Manually add a user to the "database"
        UserData user = new UserData("testUser", "testPassword", "testEmail");
        userDataAccess.createUser(user);

        //Try to add the same user using registerUser
        Exception exception = Assertions.assertThrows(RedundantDataException.class, () -> {
            userService.registerUser(user);
        });
        Assertions.assertEquals("Error: User already exists", exception.getMessage());
    }

    @Test
    @DisplayName("Login Success")
    public void loginSuccess() throws Exception {
        UserData user = new UserData("test", "password", "email");
        userDataAccess.createUser(user);

        var result = userService.loginUser(user);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(user.username(), result.username());
        Assertions.assertNotNull(result.authToken());

        Assertions.assertEquals(user, userDataAccess.getUser(user.username()));
        Assertions.assertEquals(result.authToken(), authDataAccess.getAuth(new AuthData(result.authToken(), null)).authToken());
    }

    @Test
    @DisplayName("Login Failure - incorrect password")
    public void loginFailure() throws Exception {
        UserData user = new UserData("ethan", "correct", "email");
        userDataAccess.createUser(user);

        UserData wrongUser = new UserData("ethan", "incorrect", "email");
        Exception ex = Assertions.assertThrows(UnauthorizedException.class, () -> {
            userService.loginUser(wrongUser);
        });
        Assertions.assertEquals("Error: unauthorized", ex.getMessage());
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
