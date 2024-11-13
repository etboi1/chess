package client;

import dataaccess.DataAccessException;
import dataaccess.MySqlAuthDAO;
import dataaccess.MySqlGameDAO;
import dataaccess.MySqlUserDAO;
import exception.ResponseException;
import model.UserData;
import org.junit.jupiter.api.*;
import response.LoginRegisterResponse;
import server.Server;
import server.ServerFacade;
import service.ClearService;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;
    private static MySqlUserDAO userDao;
    private static MySqlAuthDAO authDao;
    private static MySqlGameDAO gameDao;
    private static ClearService clearService;

    @BeforeAll
    public static void init() throws DataAccessException {
        server = new Server();
        var port = server.run(8080);
        String serverUrl = "http://localhost:" + port;
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(serverUrl);

        userDao = new MySqlUserDAO();
        authDao = new MySqlAuthDAO();
        gameDao = new MySqlGameDAO();
        clearService = new ClearService(userDao, authDao, gameDao);
    }

    @BeforeEach
    public void clearDatabase() throws DataAccessException {
        clearService.clearData();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    UserData goodUser = new UserData("username", "password", "email");

    @Test
    @DisplayName("Register Success ")
    public void registerSuccess() throws Exception{
        var registerResponse = facade.register("username", "password", "email");
        Assertions.assertNotNull(registerResponse);
        Assertions.assertTrue(registerResponse.authToken().length() > 10);
        Assertions.assertEquals("username", registerResponse.username());
        System.out.println(registerResponse);
    }

    //At some point, check if you need to pass the error messages from the server all the way forward
    @Test
    @DisplayName("Register Failure - both failure cases")
    public void registerFailure() throws Exception{
        //No username provided
        Exception ex = Assertions.assertThrows(ResponseException.class,
                () -> facade.register(null, "password", "email"));
        Assertions.assertEquals("failure: 400", ex.getMessage());
        //Attempt to reuse existing username
        userDao.createUser(goodUser);
        Exception redundantEx = Assertions.assertThrows(ResponseException.class,
                () -> facade.register(goodUser.username(), "newPassword", "newEmail"));
        Assertions.assertEquals("failure: 403", redundantEx.getMessage());
    }

    @Test
    @DisplayName("Login Success")
    public void loginSuccess() throws Exception {
        userDao.createUser(goodUser);

        var loginResponse = facade.login("username", "password");
        Assertions.assertNotNull(loginResponse);
        Assertions.assertTrue(loginResponse.authToken().length() > 10);
        Assertions.assertEquals("username", loginResponse.username());
    }

    //At some point, check to see if you need to have a different error for the username not already existing
    @Test
    @DisplayName("Login Failure - testing both failure responses")
    public void loginFailure() throws Exception{
        //User Doesn't Exist
        Exception ex = Assertions.assertThrows(ResponseException.class,
                () -> facade.login("username", "password"));
        Assertions.assertEquals("failure: 401", ex.getMessage());

        //Unauthorized (Wrong Password)
        userDao.createUser(goodUser);
        Exception unAuthEx = Assertions.assertThrows(ResponseException.class,
                () -> facade.login("username", "badPassword"));
        Assertions.assertEquals("failure: 401", unAuthEx.getMessage());
    }

    @Test
    public void logoutSuccess() {

    }

    @Test
    public void logoutFailure() {

    }

    @Test
    public void createGameSuccess() {

    }

    @Test
    public void createGameFailure() {

    }

    @Test
    public void listGamesSuccess() {

    }

    @Test
    public void listGamesFailure() {

    }

    @Test
    public void joinGameSuccess() {

    }

    @Test
    public void joinGameFailure() {

    }
}
