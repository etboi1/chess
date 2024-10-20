package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import response.LoginRegisterResponse;

import java.util.Objects;
import java.util.UUID;

public class AuthService {
    private final AuthDAO authDataAccess;
    private final UserDAO userDataAccess;

    public AuthService(AuthDAO authDataAccess, UserDAO userDataAccess) {
        this.authDataAccess = authDataAccess;
        this.userDataAccess = userDataAccess;
    }

    public LoginRegisterResponse loginUser(UserData user) throws Exception {
        if (userDataAccess.getUser(user.username()) == null || !Objects.equals(userDataAccess.getUser(user.username()).password(), user.password())) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        AuthData authData = new AuthData(UUID.randomUUID().toString(), user.username());
        authDataAccess.createAuth(authData);
        return new LoginRegisterResponse(authData.authToken(), user.username());
    }

    public void logoutUser(AuthData auth) throws Exception {

    }
}
