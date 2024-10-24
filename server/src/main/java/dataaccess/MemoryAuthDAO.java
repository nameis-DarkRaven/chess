package dataaccess;

import model.AuthData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {
    private Collection<AuthData> auths = new ArrayList<>();

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        auths.add(auth);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        if (auths == null){return null;}
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
        if (auths != null) {
            auths.clear();
        }
    }
}
