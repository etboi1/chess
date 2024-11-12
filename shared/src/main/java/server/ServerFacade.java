package server;

import com.google.gson.Gson;
import exception.ResponseException;
import response.CreateGameResponse;
import response.ListGamesResponse;
import response.LoginRegisterResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {serverUrl = url;}

    public LoginRegisterResponse register(String username, String password, String email) throws ResponseException {
        var path = "/user";
        return this.makeRequest("POST", path, LoginRegisterResponse.class, null, username, password, email);
    }

    public LoginRegisterResponse login(String username, String password) throws ResponseException {
        var path = "/session";
        return this.makeRequest("POST", path, LoginRegisterResponse.class, null, username, password);
    }

    public void logout(String authToken) throws ResponseException {
        var path = "/session";
        this.makeRequest("DELETE", path, null, authToken);
    }

    public CreateGameResponse createGame(String authToken, String gameName) throws ResponseException {
        var path = "/game";
        return this.makeRequest("POST", path, CreateGameResponse.class, authToken, gameName);
    }

    public ListGamesResponse listGames(String authToken) throws ResponseException {
        var path = "/game";
        return this.makeRequest("GET", path, ListGamesResponse.class, authToken);
    }

    public void joinGame(String authToken, String playerColor, String gameID) throws ResponseException {
        var path = "/game";
        this.makeRequest("PUT", path, null, authToken, playerColor, gameID);
    }

    private <T> T makeRequest(String method, String path, Class<T> responseClass, String optionalHeader, String... requestParts) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeBody(http, requestParts);
            setHeader(http, optionalHeader);
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private static void writeBody(HttpURLConnection http, String... requestParts) throws IOException {
        if (requestParts.length > 0) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = String.join("\n", requestParts);
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
            throw new ResponseException(status, "failure: " + status);
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
