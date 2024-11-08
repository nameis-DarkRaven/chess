import static org.junit.jupiter.api.Assertions.*;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import dataaccess.*;
import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import server.GameService;
import server.UserService;

import java.util.ArrayList;
import java.util.Collection;

public class SQLDAOTests {
    private static UserDAO users;
    private static AuthDAO auths;
    private static GameDAO games;

    private static UserService userService;
    private static GameService gameService;


    @BeforeAll
    public static void init() {
        users = new SQLUserDAO();
        auths = new SQLAuthDAO();
        games = new SQLGameDAO();
        userService = new UserService(users, auths);
        gameService = new GameService(auths, games, users);
    }

    @AfterEach
    public void fullClear() throws DataAccessException{
        users.clear();
        auths.clear();
        games.clear();
    }

    @Test
    public void goodCreateUserTest() throws DataAccessException {
        assertEquals(users.createUser(new UserData("user", "pass", "mail@mail.com")),
                new UserData("user", "pass", "mail@mail.com"));
    }

    @Test
    public void badCreateUserTest(){
        assertThrows(DataAccessException.class, ()->users.createUser(new UserData(null, "pass", "mail@mail.com")));
        assertThrows(DataAccessException.class, ()->users.createUser(new UserData("user", "pass", null)));
        assertThrows(DataAccessException.class, ()->users.createUser(new UserData("user", null, "mail@mail.com")));
    }

    @Test
    public void goodGetUserTest() throws DataAccessException{
        UserData user = users.createUser(new UserData("user", "pass", "mail@mail.com"));
        assertEquals(user, users.getUser(user.username()));
    }

    @Test
    public void nullGetUserTest() throws DataAccessException{
        UserData user = new UserData("user", "pass", "mail@mail.com");
        assertNull(users.getUser(user.username()));
    }

    @Test
    public void goodCreateAuthTest() throws DataAccessException{
        auths.createAuth(new AuthData("user1", userService.generateToken()));
        auths.createAuth(new AuthData("user2", userService.generateToken()));
        assertEquals(2, auths.authsSize());
    }

    @Test
    public void badCreateAuthTest(){
        assertThrows(DataAccessException.class, ()-> auths.createAuth(new AuthData(null, userService.generateToken())));
        assertThrows(DataAccessException.class, ()-> auths.createAuth(new AuthData("user2", null)));
    }

    @Test
    public void goodGetAuthTest() throws DataAccessException{
        AuthData auth = auths.createAuth(new AuthData("user1", userService.generateToken()));
        assertEquals(auth, auths.getAuth(auth.authToken()));
    }

    @Test
    public void nullGetAuthTest() throws DataAccessException {
        assertNull(auths.getAuth("wrongToken"));
    }

    @Test
    public void goodDeleteAuthTest() throws DataAccessException{
        String authToken = userService.generateToken();
        auths.createAuth(new AuthData("user1", authToken));
        auths.deleteAuth(authToken);
        assertNull(auths.getAuth(authToken));
    }

    @Test
    public void NullDeleteAuthTest() throws DataAccessException{
        String authToken = userService.generateToken();
        auths.deleteAuth(authToken);
        assertEquals(0,auths.authsSize());
    }

    @Test
    public void goodCreateGameTest() throws DataAccessException{
        int gameID = games.createGame("Game1");
        assertEquals(1,gameID);
        gameID = games.createGame("Game2");
        assertEquals(2,gameID);
    }

    @Test
    public void badCreateGameTest(){
        assertThrows(DataAccessException.class, ()-> games.createGame(null));
    }

    @Test
    public void goodGetGameTest() throws DataAccessException{
        int gameID = games.createGame("Game1");
        games.createGame("Game2");
        assertEquals(gameID, games.getGame(gameID).gameID());
    }

    @Test
    public void badGetGameTest() throws DataAccessException{
        assertNull(games.getGame(1));
    }

    @Test
    public void goodListGamesTest() throws DataAccessException{
        AuthData auth = auths.createAuth(new AuthData("user", "token"));
        Collection<GameData> expectedGamesList = new ArrayList<>();

        assertEquals(expectedGamesList, games.listGames(auth.authToken()));

        int gameID1 = games.createGame("Game1");
        int gameID2 = games.createGame("Game2");
        Collection<GameData> gamesList = games.listGames(auth.authToken());

        expectedGamesList.add(games.getGame(gameID1));
        expectedGamesList.add(games.getGame(gameID2));
        assertEquals(expectedGamesList, gamesList);
    }

    @Test
    public void badListGamesTest() throws DataAccessException{
        assertThrows(DataAccessException.class, ()-> games.listGames("token"));
        auths.createAuth(new AuthData("user","newToken"));
        assertThrows(DataAccessException.class, ()-> games.listGames("token"));
    }

    @Test
    public void goodUpdateGameTest() throws DataAccessException, InvalidMoveException {
        int gameID = games.createGame("Game1");
        GameData oldGame = games.getGame(gameID);
        ChessGame game = oldGame.game();
        game.makeMove(new ChessMove(new ChessPosition(2,1),
                new ChessPosition(3,1), null));
        GameData updatedGame = new GameData(gameID, oldGame.whiteUsername(),
                oldGame.blackUsername(), oldGame.gameName(), game);
        games.updateGame(gameID, updatedGame);
        assertEquals(updatedGame, games.getGame(gameID));

        oldGame = updatedGame;
        updatedGame = new GameData(gameID, "user", null, oldGame.gameName(), game);
        games.updateGame(gameID, updatedGame);
        assertEquals(updatedGame, games.getGame(gameID));

        AuthData auth = auths.createAuth(new AuthData("user", "auth"));
        assertEquals(1, games.listGames(auth.authToken()).size());
    }

    @DisplayName("Update game with bad input.")
    @Test
    public void badUpdateGameTest() throws DataAccessException{
        int gameID = games.createGame("Game1");
        assertThrows(DataAccessException.class, ()->games.updateGame(0,
                games.getGame(gameID)));
    }

    @DisplayName("Update game that does not exist.")
    @Test
    public void existUpdateGameTest() {
        assertThrows(DataAccessException.class, ()->games.updateGame(1, games.getGame(1)));
    }



}
