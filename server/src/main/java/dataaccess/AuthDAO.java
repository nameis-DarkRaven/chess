package dataaccess;
import exceptions.DataAccessException;
import model.AuthData;

public interface AuthDAO {
    public int authsSize()throws DataAccessException;
    public AuthData createAuth(AuthData auth) throws DataAccessException;
    public AuthData getAuth(String authToken) throws DataAccessException;
    public void deleteAuth(String authToken) throws DataAccessException;
    public void clear() throws DataAccessException;
}
