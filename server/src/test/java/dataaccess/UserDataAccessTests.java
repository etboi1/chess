package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;
import service.ClearService;
import service.UserService;
import spark.utils.Assert;

public class UserDataAccessTests {
    static private BaseMySqlDAO baseDataAccess;
    static private UserDAO userDataAccess;
    static private ClearService clearService;

    @BeforeAll
    public static void init() throws DataAccessException {
        baseDataAccess = new BaseMySqlDAO();
        userDataAccess = new MySqlUserDAO();
        AuthDAO authDataAccess = new MySqlAuthDAO();
        GameDAO gameDataAccess = new MySqlGameDAO();
        UserService userService = new UserService(authDataAccess, userDataAccess);
        clearService = new ClearService(userDataAccess, authDataAccess, gameDataAccess);
    }

    @BeforeEach
    public void clearState() throws Exception {
        clearService.clearData();
    }

    UserData goodUser = new UserData("testUser", "testPassword", "testEmail");

    @Test
    @DisplayName("Successfully Create User")
    public void createUserSuccess() throws Exception {
        userDataAccess.createUser(goodUser);
        var queryStatement = "SELECT username, password, email FROM users WHERE username=?";
        var rs = baseDataAccess.performQuery(queryStatement, goodUser.username());
        Assertions.assertTrue(rs.next());
        var storedUser = new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
        Assertions.assertEquals(goodUser.username(), storedUser.username());
        Assertions.assertTrue(BCrypt.checkpw(goodUser.password(), storedUser.password()));
        Assertions.assertEquals(goodUser.email(), storedUser.email());
    }

    @Test
    @DisplayName("Fail to Create User - email not provided")
    public void createUserFailure() {
        UserData badUser = new UserData("username", "password", null);
        Assertions.assertThrows(DataAccessException.class, () -> userDataAccess.createUser(badUser));
    }

    @Test
    @DisplayName("Successfully Get a User")
    public void getUserSuccess() throws Exception{
        var statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        baseDataAccess.performUpdate(statement, goodUser.username(), goodUser.password(), goodUser.email());
        UserData storedUser = userDataAccess.getUser(goodUser.username());
        Assertions.assertNotNull(storedUser);
        //The passwords will match in plaintext - because createUser method wasn't used, the password wasn't hashed
        Assertions.assertEquals(goodUser, storedUser);
    }

    @Test
    @DisplayName("Fail to get a User - user doesn't exist")
    public void getUserFailure() throws Exception {
        var statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        baseDataAccess.performUpdate(statement, goodUser.username(), goodUser.password(), goodUser.email());
        UserData storedUser = userDataAccess.getUser("badUsername");
        Assertions.assertNull(storedUser);
    }
}
