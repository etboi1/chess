package dataaccess;

import model.AuthData;

public interface AuthDAO {
    void clear();
    void createAuth(AuthData authData);
    AuthData getAuth(AuthData authData);
    void deleteAuth(AuthData authData);
}
