package dataaccess;
import exceptions.DataAccessException;
import model.UserData;

public interface UserDAO {
    public UserData createUser(UserData user) throws DataAccessException;
    public UserData getUser(String username) throws DataAccessException;
    public void clear() throws DataAccessException;
}
