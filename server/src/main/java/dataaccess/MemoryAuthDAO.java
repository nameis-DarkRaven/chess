package dataaccess;

import exceptions.DataAccessException;
import model.AuthData;

import java.util.ArrayList;
import java.util.Collection;

public class MemoryAuthDAO implements AuthDAO {
    private Collection<AuthData> auths = new ArrayList<>();

    @Override
    public int authsSize() throws DataAccessException {
        return auths.size();
    }

    @Override
    public AuthData createAuth(AuthData auth) throws DataAccessException {
        auths.add(auth);
        return auth;
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        if (auths == null) {
            return null;
        }
        for (AuthData auth : auths) {
            if (auth.authToken().equals(authToken)) {
                return auth;
            }
        }
        return null;
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
