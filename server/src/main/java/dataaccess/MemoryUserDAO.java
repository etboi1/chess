package dataaccess;

import model.UserData;

import java.util.HashMap;

public class MemoryUserDAO implements UserDAO{
    private final HashMap<String, UserData> users = new HashMap<>();

    @Override
    public void clear() {
        users.clear();
    }

    @Override
    public void createUser(UserData newUser) {
        users.put(newUser.username(), newUser);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }
}
