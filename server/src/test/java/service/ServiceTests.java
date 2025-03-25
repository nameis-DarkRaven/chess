package service;

import chess.ChessGame;
import exceptions.AlreadyTakenException;
import exceptions.BadRequestException;
import exceptions.DataAccessException;
import exceptions.UnauthorizedException;
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
        users = new SQLUserDAO();
        auths = new SQLAuthDAO();
        games = new SQLGameDAO();
        userService = new UserService(users, auths);
        gameService = new GameService(auths, games, users);
    }

    @AfterEach
    public void clear() throws DataAccessException {
        users.clear();
        auths.clear();
        games.clear();
    }

    @Test
    public void goodRegistration() throws DataAccessException, BadRequestException, AlreadyTakenException {
        registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
        assertEquals("user", userService.register(registerRequest).username());
    }

    @Test
    @DisplayName("Registration Error: Invalid Username or Password")
    public void invalidRegistration() throws AlreadyTakenException {
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
    public void badRegistration() throws BadRequestException {
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
    public void goodLogin() throws DataAccessException, UnauthorizedException, AlreadyTakenException, BadRequestException {

        registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
        userService.register(registerRequest);

        loginRequest = new LoginRequest("user", "pass");
        LoginResult loginResult = userService.login(loginRequest);

        assertEquals("user", loginResult.username());
        assert(userService.checkPass( users.getUser(loginResult.username()).password(), "pass"));
    }

    @Test
    @DisplayName("Login Error: Invalid Username or Password")
    public void invalidLogin() throws DataAccessException, AlreadyTakenException, BadRequestException {

        registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
        userService.register(registerRequest);
        loginRequest = new LoginRequest(null, "pass");

        UnauthorizedException e = assertThrows(UnauthorizedException.class, () -> userService.login(loginRequest));
        assertEquals("Error: Invalid username or password.", e.getMessage());
    }

    @Test
    @DisplayName("Username does not exist")
    public void badLogin() throws DataAccessException, BadRequestException, AlreadyTakenException, UnauthorizedException {
        registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
        userService.register(registerRequest);
        loginRequest = new LoginRequest("user", "wrong");

        UnauthorizedException e = assertThrows(UnauthorizedException.class, () -> userService.login(loginRequest));
        assertEquals("Error: Incorrect username or password.", e.getMessage());
    }

    @Test
    public void goodLogout() throws DataAccessException, UnauthorizedException, AlreadyTakenException, BadRequestException {
        registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
        RegisterResult registerResult = userService.register(registerRequest);


        logoutRequest = new LogoutRequest(registerResult.authToken());
        userService.logout(logoutRequest);

        assertEquals(0, auths.authsSize());
    }

    @Test
    public void badLogout() throws DataAccessException, UnauthorizedException, AlreadyTakenException, BadRequestException {
        registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
        userService.register(registerRequest);
        loginRequest = new LoginRequest("user", "pass");
        LoginResult loginResult = userService.login(loginRequest);
        logoutRequest = new LogoutRequest(loginResult.authToken());
        userService.logout(logoutRequest);

        assertNull(auths.getAuth(userService.generateToken()));
        UnauthorizedException e = assertThrows(UnauthorizedException.class, () -> userService.logout(logoutRequest));
        assertEquals("Error: Invalid logout.", e.getMessage());
    }

    @Test
    public void goodListGames() throws AlreadyTakenException, DataAccessException, BadRequestException, UnauthorizedException {
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
    }


    @Test
    public void nullListGames() throws DataAccessException, AlreadyTakenException, UnauthorizedException, BadRequestException {
        registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
        RegisterResult registerResult = userService.register(registerRequest);
        listGamesRequest = new ListGamesRequest(registerResult.authToken());
        ListGamesResult listGamesResult = gameService.listGames(listGamesRequest);

        Collection<GameData> games = new ArrayList<>();
        assertEquals(new ListGamesResult(games), listGamesResult);
    }

    @Test
    public void unauthorizedListGames() throws DataAccessException, BadRequestException, UnauthorizedException, AlreadyTakenException {
        registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
        RegisterResult registerResult = userService.register(registerRequest);
        String authToken1 = registerResult.authToken();
        String authToken2 = userService.generateToken();
        createGameRequest = new CreateGameRequest(authToken1, "Game1");
        gameService.createGame(createGameRequest);
        createGameRequest = new CreateGameRequest(authToken1, "Game2");
        gameService.createGame(createGameRequest);
        listGamesRequest = new ListGamesRequest(authToken2);

        UnauthorizedException e = assertThrows(UnauthorizedException.class, () -> gameService.listGames(listGamesRequest));
        assertEquals("Error: Unauthorized access.", e.getMessage());
    }

    @Test
    public void goodCreateGame() throws DataAccessException, BadRequestException, UnauthorizedException, AlreadyTakenException {
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
    }

    @Test
    public void badCreateGame() {
        String authToken = userService.generateToken();
        createGameRequest = new CreateGameRequest(authToken, "Game1");

        UnauthorizedException e = assertThrows(UnauthorizedException.class, () -> gameService.createGame(createGameRequest));
        assertEquals("Error: Unauthorized access.", e.getMessage());
    }

    @Test
    public void goodJoinGame() throws DataAccessException, UnauthorizedException, AlreadyTakenException, BadRequestException {
        registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
        RegisterResult registerResult = userService.register(registerRequest);

        createGameRequest = new CreateGameRequest(registerResult.authToken(), "Game1");
        CreateGameResult createGameResult = gameService.createGame(createGameRequest);

        joinGameRequest = new JoinGameRequest(registerResult.authToken(), ChessGame.TeamColor.WHITE, createGameResult.gameID());
        gameService.joinGame(joinGameRequest);

        listGamesRequest = new ListGamesRequest(registerResult.authToken());
        ListGamesResult listGamesResult = gameService.listGames(listGamesRequest);

        Collection<GameData> games = new ArrayList<>();
        games.add(new GameData(1, "user", null, "Game1", new ChessGame()));
        ListGamesResult correctResult = new ListGamesResult(games);
        assertEquals(correctResult, listGamesResult);
    }

    @Test
    public void unauthorizedJoinGame() throws DataAccessException, UnauthorizedException, BadRequestException, AlreadyTakenException {
        registerRequest = new RegisterRequest("user1", "pass1", "mail@mail.com");
        RegisterResult registerResult = userService.register(registerRequest);

        createGameRequest = new CreateGameRequest(registerResult.authToken(), "Game1");
        CreateGameResult createGameResult = gameService.createGame(createGameRequest);

        String authToken1 = userService.generateToken();
        joinGameRequest = new JoinGameRequest(authToken1, ChessGame.TeamColor.WHITE, createGameResult.gameID());


        UnauthorizedException e = assertThrows(UnauthorizedException.class, () -> gameService.joinGame(joinGameRequest));
        assertEquals("Error: Unauthorized access.", e.getMessage());
    }

    @Test
    public void playerTakenJoinGame() throws UnauthorizedException, DataAccessException, AlreadyTakenException, BadRequestException {
        registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
        RegisterResult registerResult = userService.register(registerRequest);

        createGameRequest = new CreateGameRequest(registerResult.authToken(), "Game1");
        CreateGameResult createGameResult = gameService.createGame(createGameRequest);

        joinGameRequest = new JoinGameRequest(registerResult.authToken(), ChessGame.TeamColor.WHITE, createGameResult.gameID());
        gameService.joinGame(joinGameRequest);
        joinGameRequest = new JoinGameRequest(registerResult.authToken(), ChessGame.TeamColor.WHITE, createGameResult.gameID());

        AlreadyTakenException e = assertThrows(AlreadyTakenException.class, () ->gameService.joinGame(joinGameRequest));
        assertEquals("Error: Another player has already taken that spot.", e.getMessage());
    }

    @Test
    public void badJoinGame() throws DataAccessException, BadRequestException, UnauthorizedException, AlreadyTakenException {
        registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
        RegisterResult registerResult = userService.register(registerRequest);

        createGameRequest = new CreateGameRequest(registerResult.authToken(), "Game1");
        gameService.createGame(createGameRequest);

        joinGameRequest = new JoinGameRequest(registerResult.authToken(), ChessGame.TeamColor.WHITE, 0);

        BadRequestException e = assertThrows(BadRequestException.class, () -> gameService.joinGame(joinGameRequest));
        assertEquals("Error: Invalid request.", e.getMessage());

    }
}
