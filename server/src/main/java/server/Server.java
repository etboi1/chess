package server;

import com.google.gson.Gson;
import dataaccess.*;
import model.UserData;
import service.*;
import spark.*;

import java.util.Map;

public class Server {
    private final UserDAO userDataAccess = new MemoryUserDAO();
    private final AuthDAO authDataAccess = new MemoryAuthDAO();
    private final GameDAO gameDataAccess = new MemoryGameDAO();
    private final ClearService clearService = new ClearService(userDataAccess, authDataAccess, gameDataAccess);
    private final UserService userService = new UserService(userDataAccess, authDataAccess);
    private final AuthService authService = new AuthService(authDataAccess, userDataAccess);
    private final GameService gameService = new GameService(gameDataAccess);
    private final Gson serializer = new Gson();

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::createUser);
        Spark.post("/session", this::loginUser);
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

    private Object loginUser(Request req, Response res) throws Exception{
        UserData user = serializer.fromJson(req.body(), UserData.class);
        var result = authService.loginUser(user);
        return serializer.toJson(result);
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
