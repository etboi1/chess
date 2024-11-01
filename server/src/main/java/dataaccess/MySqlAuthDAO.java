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
        var statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        super.performUpdate(statement, authData.authToken(), authData.username());
        return authData;
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        var statement = "SELECT authToken, username FROM auth where authToken=?";
        try (ResultSet rs = performQuery(statement, authToken)) {
            if (rs.next()) {
                var storedAuthToken = rs.getString("authToken");
                var storedUsername = rs.getString("username");
                return new AuthData(storedAuthToken, storedUsername);
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
