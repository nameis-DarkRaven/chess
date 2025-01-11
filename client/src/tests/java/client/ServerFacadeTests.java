package client;

import dataaccess.*;
import server.*;

import org.junit.jupiter.api.*;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    private static UserDAO users;
    private static AuthDAO auths;
    private static GameDAO games;

    private static UserService userService;


    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        users = new SQLUserDAO();
        auths = new SQLAuthDAO();
        games = new SQLGameDAO();
        userService = new UserService(users, auths);
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


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    public void sample2Test() {
        Assertions.assertTrue(true);
    }

}
