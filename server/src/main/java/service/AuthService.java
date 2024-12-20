package service;

import dataaccess.AuthDAO;
import model.AuthData;

public class AuthService {
    public final AuthDAO authDataAccess;

    public AuthService(AuthDAO authDataAccess) {
        this.authDataAccess = authDataAccess;
    }

    public AuthData authenticate(String authToken) throws Exception {
        if (authDataAccess.getAuth(authToken) == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        return authDataAccess.getAuth(authToken);
    }
}
