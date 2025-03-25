package dataaccess;

import exceptions.DataAccessException;
import model.UserData;

import java.util.ArrayList;
import java.util.Collection;

public class MemoryUserDAO implements UserDAO {
    private Collection<UserData> users = new ArrayList<>();

    @Override
    public UserData createUser(UserData user) throws DataAccessException {
        users.add(user);
        return user;
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        if (users == null){return null;}
        for (UserData user : users) {
            if (user.username().equals(username)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public void clear() throws DataAccessException {
        if (users != null) {
            users.clear();
        }
    }

}
