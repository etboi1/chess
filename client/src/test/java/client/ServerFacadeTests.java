package client;

import dataaccess.DataAccessException;
import dataaccess.MySqlAuthDAO;
import dataaccess.MySqlGameDAO;
import dataaccess.MySqlUserDAO;
import org.junit.jupiter.api.*;
import server.Server;
import service.ClearService;


public class ServerFacadeTests {

    private static Server server;
    private static ClearService clearService;

    @BeforeAll
    public static void init() throws DataAccessException {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);

        MySqlUserDAO userDao = new MySqlUserDAO();
        MySqlAuthDAO authDao = new MySqlAuthDAO();
        MySqlGameDAO gameDao = new MySqlGameDAO();
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
    public void registerSuccess() {
        Assertions.assertTrue(true);
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
