package dataaccess;

import model.UserData;

public interface UserDAO {
    void clear();
    void createUser(UserData userData);
    UserData getUser(UserData userData);
}
