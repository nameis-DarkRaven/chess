package service;

import org.eclipse.jetty.util.log.Log;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import model.*;
import server.*;
import dataaccess.*;

import javax.xml.crypto.Data;


public class ServiceTests {
//initialize services and databases

    private static UserDAO users;
    private static AuthDAO auths;
    private static GameDAO games;
    private static UserService userService;
    private static GameService gameService;
    private static RegisterRequest registerRequest;
    private static LoginRequest loginRequest;

    @BeforeAll
    public static void init() {
        users = new MemoryUserDAO();
        auths = new MemoryAuthDAO();
        games = new MemoryGameDAO();
        userService = new UserService(users, auths);
        gameService = new GameService(auths, games);
    }


    @Test
    public void goodRegistration() {
        registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
        try {
            assertEquals("user", userService.register(registerRequest).username());
        } catch (DataAccessException e) {
            assertNull(e);
        }
    }

    @Test
    @DisplayName("Registration Error: Invalid Username or Password")
    public void invalidRegistration() {
        try {
            registerRequest = new RegisterRequest(null, "pass", "mail@mail.com");
            userService.register(registerRequest);
        } catch (DataAccessException e) {
            assertEquals("Error: Invalid access.", e.getMessage());
        } catch (BadRequestException e) {
            assertEquals("Error: Invalid username or password.", e.getMessage());
        }
    }

    @Test
    @DisplayName("Username already taken")
    public void badRegistration() {
        try {
            registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
            userService.register(registerRequest);
            registerRequest = new RegisterRequest("user", "diff", "new@mail.com");
            userService.register(registerRequest);
        } catch (DataAccessException e) {
            assertEquals("Error: Invalid access.", e.getMessage());
        } catch (AlreadyTakenException e) {
            assertEquals("Error: Username already taken.", e.getMessage());
        }

    }

    @Test
    public void goodLogin() {
        loginRequest = new LoginRequest("user", "pass");
        try {
            assertEquals("user", userService.login(loginRequest).username());
            assertEquals("pass", users.getUser(userService.login(loginRequest).username()).password());
        } catch (DataAccessException e) {
            assertNull(e);
        }

    }

    @Test
    @DisplayName("Login Error: Invalid Username or Password")
    public void invalidLogin() {
        try {
            registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
            userService.register(registerRequest);
            loginRequest = new LoginRequest("wrong", "pass");

            assertEquals("user", userService.login(loginRequest).username());
            assertEquals("pass", users.getUser(userService.login(loginRequest).username()).password());
        } catch (DataAccessException e) {
            assertNull(e);
        } catch (BadRequestException e) {
            assertEquals("Error: Invalid username or password.", e.getMessage());
        }
    }

    @Test
    @DisplayName("Username does not exist")
    public void badLogin() {
        try {
            registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
            userService.register(registerRequest);
            loginRequest = new LoginRequest("user", "wrong");

            assertEquals("user", userService.login(loginRequest).username());
            assertEquals("pass", users.getUser(userService.login(loginRequest).username()).password());
        } catch (DataAccessException e) {
            assertNull(e);
        } catch (UnauthorizedException e) {
            assertEquals("Error: Incorrect username or password.", e.getMessage());
        }
    }

    @Test
    public void goodLogout() {

    }

    @Test
    public void badLogout() {

    }

    @Test
    public void goodListGames() {

    }

    @Test
    public void badListGames() {

    }

    @Test
    public void goodCreateGame() {

    }

    @Test
    public void badCreateGame() {

    }

    @Test
    public void goodJoinGame() {

    }

    @Test
    public void badJoinGame() {

    }

}
