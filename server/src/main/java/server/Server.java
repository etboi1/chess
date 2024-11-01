package server;

import com.google.gson.Gson;
import dataaccess.*;
import model.AuthData;
import model.UserData;
import request.CreateGameRequest;
import request.JoinGameRequest;
import service.*;
import spark.*;

import java.util.Map;

public class Server {
    UserDAO userDataAccess;
    AuthDAO authDataAccess;
    GameDAO gameDataAccess;

    {
        try {
            userDataAccess = new MySqlUserDAO();
            authDataAccess = new MySqlAuthDAO();
            gameDataAccess = new MySqlGameDAO();
        } catch (DataAccessException ignored) {
            
        }
    }
    final ClearService clearService = new ClearService(userDataAccess, authDataAccess, gameDataAccess);
    final UserService userService = new UserService(authDataAccess, userDataAccess);
    final GameService gameService = new GameService(gameDataAccess, authDataAccess);
    final Gson serializer = new Gson();

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::createUser);
        Spark.post("/session", this::loginUser);
        Spark.delete("/session", this::logoutUser);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::clearDatabase);
        Spark.exception(Exception.class, this::exceptionHandler);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private String createUser(Request req, Response res) throws Exception {
        UserData newUser = serializer.fromJson(req.body(), UserData.class);
        var result = userService.registerUser(newUser);
        return serializer.toJson(result);
    }

    private Object loginUser(Request req, Response res) throws Exception {
        UserData user = serializer.fromJson(req.body(), UserData.class);
        var result = userService.loginUser(user);
        return serializer.toJson(result);
    }

    private Object logoutUser(Request req, Response res) throws Exception {
        String authToken = req.headers("authorization");
        AuthData auth = new AuthData(authToken, null);
        userService.logoutUser(auth);
        res.status(200);
        return "";
    }

    private Object listGames(Request req, Response res) throws Exception {
        String authToken = req.headers("authorization");
        var result = gameService.listGames(authToken);
        return serializer.toJson(result);
    }

    private Object createGame(Request req, Response res) throws Exception {
        String authToken = req.headers("authorization");
        CreateGameRequest createRequest = serializer.fromJson(req.body(), CreateGameRequest.class);
        var result = gameService.createGame(createRequest, authToken);
        return serializer.toJson(result);
    }

    private Object joinGame(Request req, Response res) throws Exception {
        String authToken = req.headers("authorization");
        JoinGameRequest joinRequest = serializer.fromJson(req.body(), JoinGameRequest.class);
        gameService.joinGame(joinRequest, authToken);
        res.status(200);
        return "";
    }

    private Object clearDatabase(Request req, Response res) throws Exception {
        clearService.clearData();
        res.status(200);
        return "";
    }

    private void exceptionHandler(Exception ex, Request req, Response res) {
        switch (ex) {
            case BadRequestException badRequestException -> res.status(400);
            case UnauthorizedException unauthorizedException -> res.status(401);
            case RedundantDataException redundantDataException -> res.status(403);
            case null, default -> res.status(500);
        }
        res.body(serializer.toJson(Map.of("message", ex.getMessage())));
        ex.printStackTrace(System.out);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
