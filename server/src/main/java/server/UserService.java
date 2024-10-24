package server;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.UserData;
import model.AuthData;

import java.util.UUID;

public class UserService {
    private UserDAO users;
    private AuthDAO auths;

    public UserService(UserDAO users, AuthDAO auths) {
        this.users = users;
        this.auths = auths;
    }

    public String generateToken() {
        return UUID.randomUUID().toString();
    }

    public RegisterResult register(RegisterRequest request) throws DataAccessException, BadRequestException, AlreadyTakenException {
        try {
            if (request.username() == null || request.password() == null) {
                throw new BadRequestException("Error: Invalid username or password.");
            }
            UserData user = new UserData(request.username(), request.password(), request.email());
            if (users.getUser(user.username()) != null) {
                throw new AlreadyTakenException("Error: Username already taken.");
            }
            users.createUser(user);
            String authToken = generateToken();
            auths.createAuth(new AuthData(authToken, user.username()));
            AuthData auth = auths.getAuth(authToken);
            return new RegisterResult(auth.username(), auth.authToken());
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public LoginResult login(LoginRequest request) throws DataAccessException, BadRequestException, UnauthorizedException {
        try {
            if (request.username() == null || request.password() == null || users.getUser(request.username()) == null) {
                throw new BadRequestException("Error: Invalid username or password.");
            }
            if(!request.password().equals(users.getUser(request.username()).password())){
                throw new UnauthorizedException("Error: Incorrect username or password.");
            }
            String authToken = generateToken();
            auths.createAuth(new AuthData(authToken, request.username()));
            AuthData auth = auths.getAuth(authToken);
            return new LoginResult(auth.username(), auth.authToken());
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public void logout(LogoutRequest request) throws DataAccessException {
        try {
            AuthData auth = auths.getAuth(request.authToken());
            auths.deleteAuth(auth.authToken());
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }

    }
}
