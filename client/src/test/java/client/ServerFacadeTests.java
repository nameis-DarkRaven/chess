package client;

import chess.ChessGame;
import dataaccess.*;
import exceptions.*;
import requests.*;
import results.*;
import server.*;

import org.junit.jupiter.api.*;
import server.Server;
import serverfacade.ServerFacade;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;
    private static UserDAO users;
    private static AuthDAO auths;
    private static GameDAO games;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);

        serverFacade = new ServerFacade(String.format("http://localhost:%s", port));
        users = new SQLUserDAO();
        auths = new SQLAuthDAO();
        games = new SQLGameDAO();
    }

    @BeforeEach
    public void clear() throws DataAccessException {
        users.clear();
        auths.clear();
        games.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    /* Tests begin */
    @Test
    public void goodRegisterTest() throws BadRequestException {
        RegisterRequest request = new RegisterRequest("user", "pass", "email");
        assertNotNull(serverFacade.register(request));
    }

    @Test
    public void doubleRegisterTest() throws BadRequestException {
        RegisterRequest request = new RegisterRequest("user", "pass", "email");
        serverFacade.register(request);
        assertThrows(BadRequestException.class, () -> serverFacade.register(request));
    }

    @Test
    public void goodLoginTest() throws BadRequestException {
        RegisterRequest request = new RegisterRequest("user", "pass", "email");
        serverFacade.register(request);
        LoginRequest loginRequest = new LoginRequest("user", "pass");
        assertNotNull(serverFacade.login(loginRequest));
    }

    @Test
    public void badLoginTest() throws BadRequestException {
        LoginRequest loginRequest = new LoginRequest("user", "pass");
        assertThrows(BadRequestException.class, () -> serverFacade.login(loginRequest));
    }

    @Test
    public void goodLogoutTest() throws BadRequestException {
        RegisterRequest request = new RegisterRequest("user", "pass", "email");
        RegisterResult result = serverFacade.register(request);

        LogoutRequest logoutRequest = new LogoutRequest(result.authToken());
        serverFacade.logout(logoutRequest);

        CreateGameRequest createGameRequest = new CreateGameRequest(result.authToken(), "game");
        assertThrows(BadRequestException.class, () -> serverFacade.createGame(createGameRequest));
    }

    @Test
    public void badLogoutTest() {
        LogoutRequest logoutRequest = new LogoutRequest("authToken");
        assertThrows(BadRequestException.class, () -> serverFacade.logout(logoutRequest));
    }

    @Test
    public void goodCreateGameTest() throws BadRequestException {
        RegisterRequest request = new RegisterRequest("user", "pass", "email");
        RegisterResult result = serverFacade.register(request);

        CreateGameRequest createGameRequest = new CreateGameRequest(result.authToken(), "game");
        assertEquals(1, serverFacade.createGame(createGameRequest).gameID());
    }

    @Test
    public void badCreateGameTest() {
        CreateGameRequest createGameRequest = new CreateGameRequest("auth", "game");
        assertThrows(BadRequestException.class, () -> serverFacade.createGame(createGameRequest).gameID());
    }

    @Test
    public void goodListGamesTest() throws BadRequestException {
        RegisterRequest request = new RegisterRequest("user", "pass", "email");
        RegisterResult result = serverFacade.register(request);

        CreateGameRequest createGameRequest = new CreateGameRequest(result.authToken(), "game");
        serverFacade.createGame(createGameRequest);

        ListGamesRequest listGamesRequest = new ListGamesRequest(result.authToken());
        assertNotNull(serverFacade.listGames(listGamesRequest));
    }

    @Test
    public void badListGamesTest() throws BadRequestException {
        ListGamesRequest listGamesRequest = new ListGamesRequest("auth");
        assertThrows(BadRequestException.class, () -> serverFacade.listGames(listGamesRequest));
    }

    @Test
    public void goodJoinGameTest() throws BadRequestException {
        RegisterRequest request = new RegisterRequest("user", "pass", "email");
        RegisterResult result = serverFacade.register(request);

        CreateGameRequest createGameRequest = new CreateGameRequest(result.authToken(), "game");
        serverFacade.createGame(createGameRequest);

        JoinGameRequest whiteJoin = new JoinGameRequest(result.authToken(), ChessGame.TeamColor.WHITE, 1);
        assertNull(serverFacade.joinGame(whiteJoin));

        JoinGameRequest blackJoin = new JoinGameRequest(result.authToken(), ChessGame.TeamColor.BLACK, 1);
        assertNull(serverFacade.joinGame(blackJoin));
    }

    @Test
    public void unauthorizedJoinGameTest() {
        JoinGameRequest white = new JoinGameRequest("auth", ChessGame.TeamColor.WHITE, 1);
        assertThrows(BadRequestException.class, () -> serverFacade.joinGame(white));

        JoinGameRequest black = new JoinGameRequest("auth", ChessGame.TeamColor.BLACK, 1);
        assertThrows(BadRequestException.class, () -> serverFacade.joinGame(black));
    }

    @Test
    public void invalidGameIDJoinGameTest() throws BadRequestException {
        RegisterRequest request = new RegisterRequest("user", "pass", "email");
        RegisterResult result = serverFacade.register(request);

        JoinGameRequest requestWhite = new JoinGameRequest(result.authToken(), ChessGame.TeamColor.WHITE, 1);
        assertThrows(BadRequestException.class, () -> serverFacade.joinGame(requestWhite));

        JoinGameRequest requestBlack = new JoinGameRequest(result.authToken(), ChessGame.TeamColor.BLACK, 1);
        assertThrows(BadRequestException.class, () -> serverFacade.joinGame(requestBlack));
    }

}
