package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import response.LoginRegisterResponse;

import java.util.UUID;

public class UserService {
    private final UserDAO userDataAccess;
    private final AuthDAO authDataAccess;

    public UserService(UserDAO userDataAccess, AuthDAO authDataAccess) {
        this.userDataAccess = userDataAccess;
        this.authDataAccess = authDataAccess;
    }

    public LoginRegisterResponse registerUser(UserData newUser) throws Exception {
        if (userDataAccess.getUser(newUser.username()) != null) {
            throw new RedundantDataException("Error: User already exists");
        } else if (newUser.password() == null) {
            throw new BadRequestException("Error: Bad Request");
        }
        userDataAccess.createUser(newUser);
        var authData =  authDataAccess.createAuth(new AuthData(UUID.randomUUID().toString(), newUser.username()));
        return new LoginRegisterResponse(authData.authToken(), authData.username());
    }
}
