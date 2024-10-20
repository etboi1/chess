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
        userService = new UserService(userDataAccess, authDataAccess);
        clearService = new ClearService(userDataAccess, authDataAccess, gameDataAccess);
        clearService.clearData();
    }

    @Test
    @Order(1)
    @DisplayName("Normal User Registration")
    public void RegisterSuccess() throws Exception {
        UserData user = new UserData("testUser", "testPassword", "testEmail");
        var result = userService.registerUser(user);
        Assertions.assertEquals(user, result);
    }

    @Test
    @Order(2)
    @DisplayName("Registering an existing username")
    public void RegisterFailure() throws Exception {
        UserData user = new UserData("testUser", "testPassword", "testEmail");
        Exception exception = Assertions.assertThrows(RedundantDataException.class, () -> {
            userService.registerUser(user);
        });
        Assertions.assertEquals("Error: User already exists", exception.getMessage());
    }
}
