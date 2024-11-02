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
        var statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(userData.password(), BCrypt.gensalt());
        super.performUpdate(statement, userData.username(), hashedPassword, userData.email());
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        var statement = "SELECT username, password, email FROM users WHERE username=?";
        try (ResultSet rs = performQuery(statement, username)){
            if (rs.next()) {
                var storedUsername = rs.getString("username");
                var storedPassword = rs.getString("password");
                var storedEmail = rs.getString("email");
                return new UserData(storedUsername, storedPassword, storedEmail);
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        return null;
    }
}
