package dataaccess;
import model.AuthData;

public interface AuthDAO {
    public String generateToken();
    public void createAuth(AuthData auth);
    public AuthData getAuth(String authToken) throws DataAccessException;
    public void deleteAuth(String authToken) throws DataAccessException;
    public void clear();
}
