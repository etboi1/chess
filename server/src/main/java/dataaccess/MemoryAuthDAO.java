package dataaccess;

import model.AuthData;

import java.util.HashMap;

public class MemoryAuthDAO implements AuthDAO {
    private final HashMap<String, AuthData> userAuth = new HashMap<>();

    @Override
    public void clear() {
        userAuth.clear();
    }

    @Override
    public AuthData createAuth(AuthData authData) {
        userAuth.put(authData.authToken(), authData);
        return authData;
    }

    @Override
    public AuthData getAuth(String authToken) {
        return userAuth.get(authToken);
    }

    @Override
    public void deleteAuth(AuthData authData) {
        userAuth.remove(authData.authToken());
    }
}
