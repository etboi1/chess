package dataaccess;

import com.google.gson.Gson;
import model.AuthData;

import java.sql.ResultSet;
import java.sql.SQLException;

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
    public AuthData getAuth(String authToken) throws DataAccessException {
        var statement = "SELECT authToken, authData FROM auth where authToken=?";
        try (ResultSet rs = performQuery(statement, authToken)) {
            if (rs.next()) {
                return reader(rs, "authData", AuthData.class);
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        return null;
    }

    @Override
    public void deleteAuth(AuthData authData) throws DataAccessException {
        var statement = "DELETE FROM auth WHERE authToken=?";
        performUpdate(statement, authData.authToken());
    }
}
