package server;

import com.google.gson.Gson;
import dataaccess.*;
import model.AuthData;
import model.UserData;
import request.CreateGameRequest;
import service.*;
import spark.*;

import java.util.HashMap;
import java.util.Map;

public class Server {
    private final UserDAO userDataAccess = new MemoryUserDAO();
    private final AuthDAO authDataAccess = new MemoryAuthDAO();
    private final GameDAO gameDataAccess = new MemoryGameDAO();
    private final ClearService clearService = new ClearService(userDataAccess, authDataAccess, gameDataAccess);
    private final UserService userService = new UserService(authDataAccess, userDataAccess);
    private final GameService gameService = new GameService(gameDataAccess);
    private final Gson serializer = new Gson();

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Check for authentication for game endpoints before allowing them to be handled
        Spark.before("/game", this::authenticate);

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

    private void authenticate(Request req, Response res) {
        String authToken = req.headers("authorization");
        if (authToken == null || authDataAccess.getAuth(new AuthData(authToken, null)) == null) {
            Spark.halt(401, serializer.toJson(Map.of("message", "Error: unauthorized")));
        }
        Map<String, Object> reqBody;
        if (req.body() == null || req.body().isEmpty()) {
            reqBody = new HashMap<>();
        } else {
            reqBody = serializer.fromJson(req.body(), Map.class);
        }
        AuthData userAuth = authDataAccess.getAuth(new AuthData(authToken, null));
        reqBody.put("username", userAuth.username());
        serializer.toJson(reqBody);
    }

    private String createUser(Request req, Response res) throws Exception {
        UserData newUser = serializer.fromJson(req.body(), UserData.class);
        var result = userService.registerUser(newUser);
        return serializer.toJson(result);
    }

    private Object loginUser(Request req, Response res) throws Exception{
        UserData user = serializer.fromJson(req.body(), UserData.class);
        var result = userService.loginUser(user);
        return serializer.toJson(result);
    }

    private Object logoutUser(Request req, Response res) throws Exception{
        String authToken = req.headers("authorization");
        AuthData auth = new AuthData(authToken, null);
        userService.logoutUser(auth);
        res.status(200);
        return "";
    }

    private Object listGames(Request req, Response res) {
        var result = gameService.listGames();
        return serializer.toJson(result);
    }

    private Object createGame(Request req, Response res) {
        CreateGameRequest createRequest = serializer.fromJson(req.body(), CreateGameRequest.class);
        var result = gameService.createGame(createRequest);
        return serializer.toJson(result);
    }

    private Object joinGame(Request request, Response response) {
        return null;
    }

    private Object clearDatabase(Request req, Response res) throws Exception{
        clearService.clearData();
        res.status(200);
        return "";
    }

    private void exceptionHandler(Exception ex, Request req, Response res) {
        if (ex instanceof BadRequestException) {
            res.status(400);
        } else if (ex instanceof UnauthorizedException) {
            res.status(401);
        } else if (ex instanceof RedundantDataException) {
            res.status(403);
        } else {
            res.status(500);
        }
        res.body(serializer.toJson(Map.of("message", ex.getMessage())));
        ex.printStackTrace(System.out);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
