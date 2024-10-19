package server;

import com.google.gson.Gson;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import model.UserData;
import org.eclipse.jetty.server.Authentication;
import service.ServiceException;
import service.UserService;
import spark.*;

import java.util.Map;

public class Server {
    private final UserDAO userDataAccess = new MemoryUserDAO();
    private final UserService userService = new UserService(userDataAccess);
    private final Gson serializer = new Gson();

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::createUser);
        Spark.exception(Exception.class, this::exceptionHandler);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private void exceptionHandler(Exception ex, Request req, Response res) {
        res.status(500);
        res.body(serializer.toJson(Map.of("message", ex.getMessage())));
        ex.printStackTrace(System.out);
    }

    private String createUser(Request req, Response res) throws Exception {
        var newUser = serializer.fromJson(req.body(), UserData.class);
        var result = userService.registerUser(newUser);
        return serializer.toJson(result);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
