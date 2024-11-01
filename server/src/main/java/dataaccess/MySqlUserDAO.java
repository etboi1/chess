package dataaccess;

import com.google.gson.Gson;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlUserDAO extends BaseMySqlDAO implements UserDAO {
    public MySqlUserDAO() throws DataAccessException {
        super();
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE users";
        super.performUpdate(statement);
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException {
        var statement = "INSERT INTO users (username, password, email, userData) VALUES (?, ?, ?, ?)";
        var json = new Gson().toJson(userData);
        String hashedPassword = BCrypt.hashpw(userData.password(), BCrypt.gensalt());
        try {
            super.performUpdate(statement, userData.username(), hashedPassword, userData.email(), json);
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        var statement = "SELECT username, userData FROM users WHERE username=?";
        try {
            Object userData = super.performQuery(statement, this::readUser, username);
            return (UserData) userData;
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public UserData readUser(ResultSet rs) {
        String json;
        try {
            json = rs.getString("userData");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new Gson().fromJson(json, UserData.class);
    }
}
