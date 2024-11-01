package dataaccess;

import model.AuthData;

public class MySqlAuthDAO extends BaseMySqlDAO implements AuthDAO{
    public MySqlAuthDAO() throws DataAccessException {
        super();
    }

    @Override
    public void clear() {

    }

    @Override
    public AuthData createAuth(AuthData authData) {
        return null;
    }

    @Override
    public AuthData getAuth(String authToken) {
        return null;
    }

    @Override
    public void deleteAuth(AuthData authData) {

    }
}
