package service;

import dataaccess.UserDAO;
import model.UserData;

public class UserService {
    private final UserDAO userDataAccess;

    public UserService(UserDAO userDataAccess) {
        this.userDataAccess = userDataAccess;
    }

    public UserData registerUser(UserData newUser) throws ServiceException {
        if (userDataAccess.getUser(newUser.username()) != null) {
            throw new ServiceException("User already exists");
        }
    }
}
