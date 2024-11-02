package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;
import service.AuthService;
import service.ClearService;

import java.sql.ResultSet;

public class AuthDataAccessTests {
    static private BaseMySqlDAO baseDataAccess;
    static private AuthDAO authDataAccess;
    static private ClearService clearService;

    @BeforeAll
    public static void init() throws DataAccessException {
        baseDataAccess = new BaseMySqlDAO();
        UserDAO userDataAccess = new MySqlUserDAO();
        authDataAccess = new MySqlAuthDAO();
        GameDAO gameDataAccess = new MySqlGameDAO();
        AuthService authService = new AuthService(authDataAccess);
        clearService = new ClearService(userDataAccess, authDataAccess, gameDataAccess);
    }

    @BeforeEach
    public void clearState() throws DataAccessException {
        clearService.clearData();
    }

    AuthData authData = new AuthData("authToken", "username");

    @Test
    @DisplayName("Create Auth Success")
    public void createAuthSuccess() throws Exception {
        authDataAccess.createAuth(authData);
        var statement = "SELECT authToken, username FROM auth where authToken=?";
        ResultSet rs = baseDataAccess.performQuery(statement, authData.authToken());
        Assertions.assertTrue(rs.next());
        var storedAuthToken = rs.getString("authToken");
        Assertions.assertEquals(storedAuthToken, authData.authToken());
    }

    @Test
    @DisplayName("Auth Creation Failure - null username")
    public void createAuthFailure() {
        AuthData badAuth = new AuthData("authToken", null);
        Assertions.assertThrows(DataAccessException.class, () -> authDataAccess.createAuth(badAuth));
    }

    @Test
    @DisplayName("Successfully Retrieve Auth")
    public void getAuthSuccess() throws Exception {
        authDataAccess.createAuth(authData);
        Assertions.assertEquals(authData, authDataAccess.getAuth(authData.authToken()));
    }

    @Test
    @DisplayName("Fail to Retrieve Auth - incorrect authToken")
    public void getAuthFailure() throws Exception {
        authDataAccess.createAuth(authData);
        Assertions.assertNull(authDataAccess.getAuth("badAuthToken"));
    }

    @Test
    @DisplayName("Successfully Delete Auth")
    public void deleteAuthSuccess() throws Exception {
        authDataAccess.createAuth(authData);
        Assertions.assertEquals(authData, authDataAccess.getAuth(authData.authToken()));
        authDataAccess.deleteAuth(authData);
        Assertions.assertNull(authDataAccess.getAuth(authData.authToken()));
    }

    @Test
    @DisplayName("Fail to Delete Auth - nonexistent authToken")
    public void deleteAuthFailure() throws Exception {
        authDataAccess.createAuth(authData);
        authDataAccess.deleteAuth(new AuthData("badAuth", "username"));
        Assertions.assertEquals(authData, authDataAccess.getAuth(authData.authToken()));
    }
}
