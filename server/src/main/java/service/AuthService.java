package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;

public class AuthService {
    private final AuthDAO authDataAccess;

    public AuthService(AuthDAO authDataAccess) {
        this.authDataAccess = authDataAccess;
    }
}
