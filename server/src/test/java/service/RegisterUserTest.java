package service;

import dataaccess.*;
import model.UserData;
import org.junit.jupiter.api.*;
import response.LoginRegisterResponse;

public class RegisterUserTest {
    static private UserDAO userDataAccess;
    static private AuthDAO authDataAccess;
    static private GameDAO gameDataAccess;
    static private UserService userService;
    static private ClearService clearService;

    @BeforeAll
    public static void init() throws Exception {
        userDataAccess = new MemoryUserDAO();
        authDataAccess = new MemoryAuthDAO();
        gameDataAccess = new MemoryGameDAO();
        userService = new UserService(authDataAccess, userDataAccess);
        clearService = new ClearService(userDataAccess, authDataAccess, gameDataAccess);
        clearService.clearData();
    }

    @Test
    @DisplayName("Normal User Registration")
    public void RegisterSuccess() throws Exception {
        UserData user = new UserData("testUser", "testPassword", "testEmail");
        var result = userService.registerUser(user);

        //Check that the response object isn't null, has the correct username, and has an authToken
        Assertions.assertNotNull(result);
        Assertions.assertEquals(user.username(), result.username());
        Assertions.assertNotNull(result.authToken());
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
}
