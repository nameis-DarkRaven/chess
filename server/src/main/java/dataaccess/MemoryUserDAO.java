package dataaccess;

import model.UserData;

import java.util.Collection;

public class MemoryUserDAO implements UserDAO{
    private Collection<UserData> users;
    @Override
    public void createUser(UserData user) {
        users.add(user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        for(UserData user: users){
            if (user.username() == username){
                return user;
            }
        }
        throw new DataAccessException("User does not exist.");
    }

    @Override
    public void clear() {
        users.clear();
    }

}
