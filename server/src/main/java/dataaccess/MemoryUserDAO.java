package dataaccess;

import model.UserData;

import java.util.Collection;

public class MemoryUserDAO implements UserDAO {
    private Collection<UserData> users;

    @Override
    public void createUser(UserData user) throws DataAccessException {
        users.add(user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
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
