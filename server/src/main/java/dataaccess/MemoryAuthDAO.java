package dataaccess;

import model.AuthData;

import java.util.Collection;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {
    private Collection<AuthData> auths;

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        auths.add(auth);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        for (AuthData auth : auths) {
            if (auth.authToken() == authToken) {
                return auth;
            }
        }
        throw new DataAccessException("Invalid authorization.");
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        AuthData auth = getAuth(authToken);
        auths.remove(auth);
    }

    @Override
    public void clear() throws DataAccessException {
        auths.clear();
    }
}
