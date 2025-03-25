package server;

import dataaccess.*;
import exceptions.AlreadyTakenException;
import exceptions.BadRequestException;
import exceptions.DataAccessException;
import exceptions.UnauthorizedException;
import model.*;
import org.mindrot.jbcrypt.BCrypt;
import requests.*;
import results.*;

import java.util.UUID;

public class UserService {
    private final UserDAO users;
    private final AuthDAO auths;

    public UserService(UserDAO users, AuthDAO auths) {
        this.users = users;
        this.auths = auths;
    }

    public String hashPass(String pass){
        return BCrypt.hashpw(pass, BCrypt.gensalt());
    }

    public boolean checkPass(String storedPass, String pass){
        return BCrypt.checkpw(pass, storedPass);
    }

    //generates an authorization token
    public String generateToken() {
        return UUID.randomUUID().toString();
    }

    public RegisterResult register(RegisterRequest request)
            throws DataAccessException, BadRequestException, AlreadyTakenException {
        try {
            if (request.username() == null || request.password() == null) {
                throw new BadRequestException("Error: Invalid username or password.");
            }
            UserData user = new UserData(request.username(), hashPass(request.password()), request.email());
            if (users.getUser(user.username()) != null) {
                throw new AlreadyTakenException("Error: Username already taken.");
            }
            users.createUser(user);
            String authToken = generateToken();
            auths.createAuth(new AuthData(user.username(),authToken));
            AuthData auth = auths.getAuth(authToken);
            return new RegisterResult(auth.username(), auth.authToken());
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public LoginResult login(LoginRequest request) throws DataAccessException, UnauthorizedException {
        try {
            if (request.username() == null || request.password() == null) {
                throw new UnauthorizedException("Error: Invalid username or password.");
            }
            if(users.getUser(request.username()) == null ||
                    !checkPass(users.getUser(request.username()).password(), request.password())){
                throw new UnauthorizedException("Error: Incorrect username or password.");
            }
            String authToken = generateToken();
            auths.createAuth(new AuthData(request.username(), authToken));
            AuthData auth = auths.getAuth(authToken);
            return new LoginResult(auth.username(), auth.authToken());
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public void logout(LogoutRequest request) throws DataAccessException, UnauthorizedException {
        try {
            AuthData auth = auths.getAuth(request.authToken());
            if(auth != null){
            auths.deleteAuth(auth.authToken());}
            else{
                throw new UnauthorizedException("Error: Invalid logout.");
            }
        } catch (DataAccessException e) {
            throw new UnauthorizedException("Error: Invalid logout.");
        }

    }
}
