package client;

import dataaccess.DataAccessException;
import dataaccess.MySqlAuthDAO;
import dataaccess.MySqlGameDAO;
import dataaccess.MySqlUserDAO;
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


    @Test
    public void registerSuccess() throws Exception{
        var registerResponse = facade.register("username", "password", "email");
        Assertions.assertNotNull(registerResponse);
        Assertions.assertTrue(registerResponse.authToken().length() > 10);
        Assertions.assertEquals("username", registerResponse.username());
    }

    @Test
    public void registerFailure() {

    }

    @Test
    public void loginSuccess() {

    }

    @Test
    public void loginFailure() {

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
