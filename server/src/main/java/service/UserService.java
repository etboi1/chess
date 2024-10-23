package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import response.LoginRegisterResponse;

import java.util.Objects;
import java.util.UUID;

public class UserService extends AuthService {
    private final UserDAO userDataAccess;

    public UserService(AuthDAO authDataAccess, UserDAO userDataAccess) {
        super(authDataAccess);
        this.userDataAccess = userDataAccess;
    }

    public LoginRegisterResponse registerUser(UserData newUser) throws Exception {
        if (userDataAccess.getUser(newUser.username()) != null) {
            throw new RedundantDataException("Error: User already exists");
        } else if (newUser.password() == null) {
            throw new BadRequestException("Error: Bad Request");
        }
        userDataAccess.createUser(newUser);
        var authData = authDataAccess.createAuth(new AuthData(UUID.randomUUID().toString(), newUser.username()));
        return new LoginRegisterResponse(authData.authToken(), authData.username());
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
        super.authenticate(auth.authToken());
        authDataAccess.deleteAuth(auth);
    }
}
