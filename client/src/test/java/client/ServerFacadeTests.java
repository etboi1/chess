package client;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MySqlAuthDAO;
import dataaccess.MySqlGameDAO;
import dataaccess.MySqlUserDAO;
import exception.ResponseException;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import response.LoginRegisterResponse;
import server.Server;
import server.ServerFacade;
import service.ClearService;
import service.UserService;

import java.util.ArrayList;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;
    private static MySqlUserDAO userDao;
    private static MySqlAuthDAO authDao;
    private static MySqlGameDAO gameDao;
    private static UserService userService;
    private static ClearService clearService;

    @BeforeAll
    public static void init() throws DataAccessException {
        server = new Server();
        var port = server.run(0);
        String serverUrl = "http://localhost:" + port;
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(serverUrl);

        userDao = new MySqlUserDAO();
        authDao = new MySqlAuthDAO();
        gameDao = new MySqlGameDAO();
        userService = new UserService(authDao, userDao);
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
        Assertions.assertEquals("Error: Bad Request\n", ex.getMessage());
        //Attempt to reuse existing username
        userDao.createUser(goodUser);
        Exception redundantEx = Assertions.assertThrows(ResponseException.class,
                () -> facade.register(goodUser.username(), "newPassword", "newEmail"));
        Assertions.assertEquals("Error: User already exists\n", redundantEx.getMessage());
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
        Assertions.assertEquals("Error: unauthorized\n", ex.getMessage());

        //Unauthorized (Wrong Password)
        userDao.createUser(goodUser);
        Exception unAuthEx = Assertions.assertThrows(ResponseException.class,
                () -> facade.login("username", "badPassword"));
        Assertions.assertEquals("Error: unauthorized\n", unAuthEx.getMessage());
    }

    @Test
    @DisplayName("Logout Success")
    public void logoutSuccess() throws Exception{
        var registerRes = userService.registerUser(goodUser);
        Assertions.assertEquals(registerRes.authToken(), authDao.getAuth(registerRes.authToken()).authToken());
        facade.logout(registerRes.authToken());
        Assertions.assertNull(authDao.getAuth(registerRes.authToken()));
    }

    @Test
    @DisplayName("Logout Failure - unauthorized (incorrect auth)")
    public void logoutFailure() throws Exception {
        var registerRes = userService.registerUser(goodUser);
        Assertions.assertEquals(registerRes.authToken(), authDao.getAuth(registerRes.authToken()).authToken());
        Exception ex = Assertions.assertThrows(ResponseException.class,
                () -> facade.logout("badAuthToken"));
        Assertions.assertEquals("Error: unauthorized\n", ex.getMessage());
    }

    @Test
    @DisplayName("Create Game Success")
    public void createGameSuccess() throws Exception {
        var registerRes = userService.registerUser(goodUser);
        var createRes = facade.createGame(registerRes.authToken(), "gameName");
        Assertions.assertNotNull(createRes);
        Assertions.assertInstanceOf(Integer.class, createRes.gameID());
        var storedGame = gameDao.getGame(createRes.gameID());
        Assertions.assertNotNull(storedGame);
        Assertions.assertEquals("gameName", storedGame.gameName());
    }

    @Test
    @DisplayName("Create Game Failure - test unauthorized and null gameName exceptions")
    public void createGameFailure() throws Exception {
        //Unauthorized
        var registerRes = userService.registerUser(goodUser);
        Exception ex = Assertions.assertThrows(ResponseException.class,
                () -> facade.createGame("badAuthToken", "gameName"));
        Assertions.assertEquals("Error: unauthorized\n", ex.getMessage());
        //Trying to create a game with no name
        Exception badNameEx = Assertions.assertThrows(ResponseException.class,
                () -> facade.createGame(registerRes.authToken(), null));
        Assertions.assertEquals("Error: bad request\n", badNameEx.getMessage());
    }

    GameData goodGame = new GameData(null, null, null, "gameName", new ChessGame());

    @Test
    @DisplayName("List Games Success - also checks no games")
    public void listGamesSuccess() throws Exception {
        var registerRes = userService.registerUser(goodUser);
        //Ensure an empty list is passed back when no games are stored
        var listRes = facade.listGames(registerRes.authToken());
        Assertions.assertNotNull(listRes);
        Assertions.assertInstanceOf(ArrayList.class, listRes.games());
        Assertions.assertTrue(listRes.games().isEmpty());
        //Now ensure that a list of games is passed back when present
        gameDao.createGame(goodGame);
        Assertions.assertEquals(goodGame.gameName(), gameDao.getGame(1).gameName());
        listRes = facade.listGames(registerRes.authToken());
        Assertions.assertNotNull(listRes);
        Assertions.assertEquals(goodGame.gameName(), listRes.games().getFirst().gameName());
    }

    @Test
    @DisplayName("List Games Failure - unauthorized")
    public void listGamesFailure() throws Exception{
        var registerRes = userService.registerUser(goodUser);
        Exception ex = Assertions.assertThrows(ResponseException.class,
                () -> facade.listGames("badAuthToken"));
        Assertions.assertEquals("Error: unauthorized\n", ex.getMessage());
    }

    @Test
    @DisplayName("Join Game Success")
    public void joinGameSuccess() throws Exception{
        var registerRes = userService.registerUser(goodUser);
        gameDao.createGame(goodGame);
        var storedGame = gameDao.getGame(1);
        Assertions.assertNull(storedGame.whiteUsername());
        facade.joinGame(registerRes.authToken(), "WHITE", 1);
        var updatedGame = gameDao.getGame(1);
        Assertions.assertEquals(updatedGame.whiteUsername(), registerRes.username());
    }

    @Test
    @DisplayName("Join Game Failure - Join Filled Spot or Invalid Request")
    public void joinGameFailure() throws Exception {
        var registerRes = userService.registerUser(goodUser);
        gameDao.createGame(
                new GameData(null, "user1", "user2", "gameName", new ChessGame())
        );
        Exception ex = Assertions.assertThrows(ResponseException.class,
                () -> facade.joinGame(registerRes.authToken(), "WHITE", 1));
        Assertions.assertEquals("Error: already taken\n", ex.getMessage());
        Exception badReqEx = Assertions.assertThrows(ResponseException.class,
                () -> facade.joinGame(registerRes.authToken(), "white", 1));
        Assertions.assertEquals("Error: bad request\n", badReqEx.getMessage());
    }

    @Test
    @DisplayName("Test All ServerFacade Methods - ensures no consistency errors")
    public void testAll() throws Exception{
        var registerRes = facade.register(goodUser.username(), goodUser.password(), goodUser.email());
        Assertions.assertNotNull(registerRes);
        Assertions.assertEquals(goodUser.username(), registerRes.username());

        facade.logout(registerRes.authToken());
        Assertions.assertNull(authDao.getAuth(registerRes.authToken()));

        var loginRes = facade.login(goodUser.username(), goodUser.password());
        Assertions.assertNotNull(loginRes);
        Assertions.assertEquals(goodUser.username(), loginRes.username());

        var createRes = facade.createGame(loginRes.authToken(), goodGame.gameName());
        var game1 = gameDao.getGame(createRes.gameID());
        Assertions.assertNotNull(game1);
        Assertions.assertEquals(createRes.gameID(), game1.gameID());

        var listGamesRes = facade.listGames(loginRes.authToken());
        Assertions.assertEquals(1, listGamesRes.games().size());
        Assertions.assertEquals(game1, listGamesRes.games().getFirst());

        facade.joinGame(loginRes.authToken(), "WHITE", game1.gameID());
        game1 = gameDao.getGame(game1.gameID());
        Assertions.assertEquals(loginRes.username(), game1.whiteUsername());

        facade.logout(loginRes.authToken());
        UserData secondUser = new UserData("secondUser", "secondPassword", "secondEmail");
        var registerRes2 = facade.register(secondUser.username(), secondUser.password(), secondUser.email());

        var createRes2 = facade.createGame(registerRes2.authToken(), "secondGame");

        listGamesRes = facade.listGames(registerRes2.authToken());
        Assertions.assertEquals(2, listGamesRes.games().size());

        facade.joinGame(registerRes2.authToken(), "BLACK", game1.gameID());
        game1 = gameDao.getGame(game1.gameID());
        Assertions.assertEquals(registerRes2.username(), game1.blackUsername());
    }
}
