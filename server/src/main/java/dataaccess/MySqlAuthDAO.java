package dataaccess;

import com.google.gson.Gson;
import model.AuthData;

public class MySqlAuthDAO extends BaseMySqlDAO implements AuthDAO{
    public MySqlAuthDAO() throws DataAccessException {
        super();
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE auth";
        super.performUpdate(statement);
    }

    @Override
    public AuthData createAuth(AuthData authData) throws DataAccessException {
        var statement = "INSERT INTO auth (authToken, username, authData) VALUES (?, ?, ?)";
        var json = new Gson().toJson(authData);
        super.performUpdate(statement, authData.authToken(), authData.username(), json);
        return authData;
    }

    @Override
    public AuthData getAuth(String authToken) {
        return null;
    }

    @Override
    public void deleteAuth(AuthData authData) {

    }
}
