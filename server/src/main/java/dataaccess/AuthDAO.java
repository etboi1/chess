package dataaccess;

import model.AuthData;

public interface AuthDAO {
    void clear();
    AuthData createAuth(AuthData authData);
    AuthData getAuth(AuthData authData);
    void deleteAuth(AuthData authData);
}
