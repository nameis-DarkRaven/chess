package service;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import requests.*;
import results.*;
import server.*;
import dataaccess.*;

import java.util.ArrayList;
import java.util.Collection;


public class ServiceTests {
//initialize services and databases

    private static UserDAO users;
    private static AuthDAO auths;
    private static GameDAO games;

    private static UserService userService;
    private static GameService gameService;

    private static RegisterRequest registerRequest;
    private static LoginRequest loginRequest;
    private static LogoutRequest logoutRequest;
    private static ListGamesRequest listGamesRequest;
    private static CreateGameRequest createGameRequest;
    private static JoinGameRequest joinGameRequest;

    @BeforeAll
    public static void init() {
        users = new MemoryUserDAO();
        auths = new MemoryAuthDAO();
        games = new MemoryGameDAO();
        userService = new UserService(users, auths);
        gameService = new GameService(auths, games, users);
    }

    @BeforeEach
    public void setup() {
        try {
            users.clear();
            auths.clear();
            games.clear();
        } catch (DataAccessException _) {
        }
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
        try {
            registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
            userService.register(registerRequest);

            loginRequest = new LoginRequest("user", "pass");
            LoginResult loginResult = userService.login(loginRequest);

            assertEquals("user", loginResult.username());
            assertEquals("pass", users.getUser(loginResult.username()).password());
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
            loginRequest = new LoginRequest(null, "pass");
            LoginResult loginResult = userService.login(loginRequest);

            assertEquals("", loginResult.username());
            assertEquals("", users.getUser(loginResult.username()).password());
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
            LoginResult loginResult = userService.login(loginRequest);

            assertEquals("", loginResult.username());
            assertEquals("", users.getUser(loginResult.username()).password());
        } catch (DataAccessException e) {
            assertNull(e);
        } catch (UnauthorizedException e) {
            assertEquals("Error: Incorrect username or password.", e.getMessage());
        }
    }

    @Test
    public void goodLogout() {
        try {
            registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
            RegisterResult registerResult = userService.register(registerRequest);


            logoutRequest = new LogoutRequest(registerResult.authToken());
            userService.logout(logoutRequest);

            assertEquals(0, auths.authsSize());
        } catch (DataAccessException e) {
            assertNull(e.getMessage());
        }
    }

    @Test
    public void badLogout() {
        try {
            registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
            userService.register(registerRequest);
            loginRequest = new LoginRequest("user", "pass");
            LoginResult loginResult = userService.login(loginRequest);
            logoutRequest = new LogoutRequest(loginResult.authToken());
            userService.logout(logoutRequest);

            assertNull(auths.getAuth(userService.generateToken()));
        } catch (DataAccessException e) {
            assertEquals("Invalid authorization.", e.getMessage());
        }

    }

    @Test
    public void goodListGames() {
        try {
            registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
            RegisterResult registerResult = userService.register(registerRequest);
            String authToken1 = registerResult.authToken();
            createGameRequest = new CreateGameRequest(authToken1, "Game1");
            gameService.createGame(createGameRequest);
            createGameRequest = new CreateGameRequest(authToken1, "Game2");
            gameService.createGame(createGameRequest);
            listGamesRequest = new ListGamesRequest(registerResult.authToken());
            ListGamesResult listGamesResult = gameService.listGames(listGamesRequest);

            Collection<GameData> games = new ArrayList<>();
            games.add(new GameData(1, null, null, "Game1", new ChessGame()));
            games.add(new GameData(2, null, null, "Game2", new ChessGame()));
            ListGamesResult correctResult = new ListGamesResult(games);
            assertEquals(correctResult, listGamesResult);

        } catch (DataAccessException e) {
            assertNull(e);
        }
    }


    @Test
    public void nullListGames() {
        try {
            registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
            RegisterResult registerResult = userService.register(registerRequest);
            listGamesRequest = new ListGamesRequest(registerResult.authToken());
            ListGamesResult listGamesResult = gameService.listGames(listGamesRequest);

            Collection<GameData> games = new ArrayList<>();
            assertEquals(new ListGamesResult(games), listGamesResult);

        } catch (DataAccessException e) {
            assertNull(e);
        }
    }

    @Test
    public void UnauthorizedListGames() {
        try {
            String authToken1 = userService.generateToken();
            createGameRequest = new CreateGameRequest(authToken1, "Game1");
            gameService.createGame(createGameRequest);
            createGameRequest = new CreateGameRequest(authToken1, "Game2");
            gameService.createGame(createGameRequest);
            listGamesRequest = new ListGamesRequest(authToken1);
            ListGamesResult listGamesResult = gameService.listGames(listGamesRequest);

            assertNull(listGamesResult);

        } catch (UnauthorizedException e) {
            assertEquals("Error: Unauthorized access.", e.getMessage());
        } catch (DataAccessException e) {
            assertNull(e);
        }
    }

    @Test
    public void goodCreateGame() {
        try {
            registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
            RegisterResult registerResult = userService.register(registerRequest);
            createGameRequest = new CreateGameRequest(registerResult.authToken(), "Game1");
            gameService.createGame(createGameRequest);
            listGamesRequest = new ListGamesRequest(registerResult.authToken());
            ListGamesResult listGamesResult = gameService.listGames(listGamesRequest);

            Collection<GameData> games = new ArrayList<>();
            games.add(new GameData(1, null, null, "Game1", new ChessGame()));
            ListGamesResult correctResult = new ListGamesResult(games);
            assertEquals(correctResult, listGamesResult);

        } catch (DataAccessException e) {
            assertNull(e);
        }


    }

    @Test
    public void badCreateGame() {
        try {
            String authToken = userService.generateToken();
            createGameRequest = new CreateGameRequest(authToken, "Game1");
            gameService.createGame(createGameRequest);
            listGamesRequest = new ListGamesRequest(authToken);
            ListGamesResult listGamesResult = gameService.listGames(listGamesRequest);

            assertNull(listGamesResult);

        } catch (UnauthorizedException e) {
            assertEquals("Error: Unauthorized access.", e.getMessage());
        } catch (DataAccessException e) {
            assertNull(e);
        }
    }

    @Test
    public void goodJoinGame() {
        try {
            registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
            RegisterResult registerResult = userService.register(registerRequest);

            createGameRequest = new CreateGameRequest(registerResult.authToken(), "Game1");
            CreateGameResult createGameResult = gameService.createGame(createGameRequest);

            joinGameRequest = new JoinGameRequest(registerResult.authToken(), ChessGame.TeamColor.WHITE, createGameResult.gameID());
            JoinGameResult joinGameResult = gameService.joinGame(joinGameRequest);

            listGamesRequest = new ListGamesRequest(registerResult.authToken());
            ListGamesResult listGamesResult = gameService.listGames(listGamesRequest);

            Collection<GameData> games = new ArrayList<>();
            games.add(new GameData(1, "user", null, "Game1", new ChessGame()));
            ListGamesResult correctResult = new ListGamesResult(games);
            assertEquals(correctResult, listGamesResult);

        } catch (DataAccessException e){
            assertNull(e);
        }
    }

    @Test
    public void unauthorizedJoinGame() {
        try {
            registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
            RegisterResult registerResult = userService.register(registerRequest);

            createGameRequest = new CreateGameRequest(registerResult.authToken(), "Game1");
            CreateGameResult createGameResult = gameService.createGame(createGameRequest);

            String authToken1 = userService.generateToken();
            joinGameRequest = new JoinGameRequest(authToken1, ChessGame.TeamColor.WHITE, createGameResult.gameID());
            gameService.joinGame(joinGameRequest);

            listGamesRequest = new ListGamesRequest(registerResult.authToken());
            ListGamesResult listGamesResult = gameService.listGames(listGamesRequest);

            assertNull(listGamesResult);

        } catch (DataAccessException e){
            assertNull(e);
        } catch (UnauthorizedException e){
            assertEquals("Error: Unauthorized access.", e.getMessage());
        }
    }

    @Test
    public void playerTakenJoinGame() {
        try {
            registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
            RegisterResult registerResult = userService.register(registerRequest);

            createGameRequest = new CreateGameRequest(registerResult.authToken(), "Game1");
            CreateGameResult createGameResult = gameService.createGame(createGameRequest);

            joinGameRequest = new JoinGameRequest(registerResult.authToken(), ChessGame.TeamColor.WHITE, createGameResult.gameID());
            gameService.joinGame(joinGameRequest);
            joinGameRequest = new JoinGameRequest(registerResult.authToken(), ChessGame.TeamColor.WHITE, createGameResult.gameID());
            gameService.joinGame(joinGameRequest);

            listGamesRequest = new ListGamesRequest(registerResult.authToken());
            ListGamesResult listGamesResult = gameService.listGames(listGamesRequest);

            assertNull(listGamesResult);

        } catch (DataAccessException e){
            assertNull(e);
        } catch (AlreadyTakenException e){
            assertEquals("Error: Another player has already taken that spot.", e.getMessage());
        }
    }

    @Test
    public void badJoinGame() {
        try {
            registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
            RegisterResult registerResult = userService.register(registerRequest);

            createGameRequest = new CreateGameRequest(registerResult.authToken(), "Game1");
            CreateGameResult createGameResult = gameService.createGame(createGameRequest);

            joinGameRequest = new JoinGameRequest(registerResult.authToken(), ChessGame.TeamColor.WHITE, 0);
            JoinGameResult joinGameResult = gameService.joinGame(joinGameRequest);

            listGamesRequest = new ListGamesRequest(registerResult.authToken());
            ListGamesResult listGamesResult = gameService.listGames(listGamesRequest);

            assertNull(listGamesResult);

        } catch (DataAccessException e){
            assertNull(e);
        } catch (BadRequestException e){
            assertEquals("Error: Invalid request.", e.getMessage());
        }
    }


}
