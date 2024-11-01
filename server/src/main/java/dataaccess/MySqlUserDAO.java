package dataaccess;

import model.UserData;

public class MySqlUserDAO extends BaseMySqlDAO implements UserDAO {
    public MySqlUserDAO() throws DataAccessException {
        super();
    }

    @Override
    public void clear() {

    }

    @Override
    public void createUser(UserData userData) {

    }

    @Override
    public UserData getUser(String username) {
        return null;
    }
}
