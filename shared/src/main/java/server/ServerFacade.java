package server;

import com.google.gson.Gson;
import exception.ResponseException;
import model.UserData;
import request.CreateGameRequest;
import request.JoinGameRequest;
import response.CreateGameResponse;
import response.ListGamesResponse;
import response.LoginRegisterResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.Map;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {serverUrl = url;}

    public LoginRegisterResponse register(String username, String password, String email) throws ResponseException {
        var path = "/user";
        UserData registerReq = new UserData(username, password, email);
        return this.makeRequest("POST", path, LoginRegisterResponse.class, null, registerReq);
    }

    public LoginRegisterResponse login(String username, String password) throws ResponseException {
        var path = "/session";
        UserData loginReq = new UserData(username, password, null);
        return this.makeRequest("POST", path, LoginRegisterResponse.class, null, loginReq);
    }

    public void logout(String authToken) throws ResponseException {
        var path = "/session";
        this.makeRequest("DELETE", path, null, authToken, null);
    }

    public CreateGameResponse createGame(String authToken, String gameName) throws ResponseException {
        var path = "/game";
        CreateGameRequest createReq = new CreateGameRequest(gameName);
        return this.makeRequest("POST", path, CreateGameResponse.class, authToken, createReq);
    }

    public ListGamesResponse listGames(String authToken) throws ResponseException {
        var path = "/game";
        return this.makeRequest("GET", path, ListGamesResponse.class, authToken, null);
    }

    public void joinGame(String authToken, String playerColor, Integer gameID) throws ResponseException {
        var path = "/game";
        JoinGameRequest joinReq = new JoinGameRequest(playerColor, gameID);
        this.makeRequest("PUT", path, null, authToken, joinReq);
    }

    private <T> T makeRequest(String method, String path, Class<T> responseClass, String optionalHeader, Object req) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            setHeader(http, optionalHeader);
            writeBody(http, req);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private static void writeBody(HttpURLConnection http, Object req) throws IOException {
        if (req != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(req);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private static void setHeader(HttpURLConnection http, String optionalHeader) {
        if (optionalHeader != null && !optionalHeader.isEmpty()) {
            http.addRequestProperty("authorization", optionalHeader);
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            //Check for error message from the server before throwing generic error
            try (InputStream respBody = http.getErrorStream()) {
                if (respBody != null) {
                    InputStreamReader reader = new InputStreamReader(respBody);
                    Map jsonMap = new Gson().fromJson(reader, Map.class);
                    String errorMessage = (String) jsonMap.get("message");
                    throw new ResponseException(status, errorMessage + "\n");
                }
            }
            throw new ResponseException(status, "failure: " + status + "\n");
        }
    }

    private static <T> T readBody (HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private boolean isSuccessful(int status) { return status / 100 == 2; }
}
