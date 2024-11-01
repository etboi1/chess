package dataaccess;

import model.AuthData;

public interface AuthDAO {
    void clear() throws DataAccessException;

    AuthData createAuth(AuthData authData) throws DataAccessException;

    AuthData getAuth(String authToken) throws DataAccessException;

    void deleteAuth(AuthData authData) throws DataAccessException;
}
