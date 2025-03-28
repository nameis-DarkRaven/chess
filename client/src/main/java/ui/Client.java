package ui;

import chess.ChessGame;
import com.google.gson.Gson;
import exceptions.BadRequestException;
import model.*;
import requests.*;
import results.*;
import serverfacade.ServerFacade;

import java.util.Arrays;

public class Client {
    private final ServerFacade server;
    private final String serverURL;
    private State state=State.loggedOut;
    private UserData user;
    private AuthData auth;
    private InGameClient inGame;

    public Client(String serverURL) {
        server=new ServerFacade(serverURL);
        this.serverURL=serverURL;
    }

    public String eval(String input) {
        try {
            var tokens=input.toLowerCase().split(" ");
            var cmd=(tokens.length > 0) ? tokens[0] : "help";
            var params=Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> logIn(params);
                case "logout" -> logOut();
                case "listGames" -> listGames();
                case "createGame" -> createGame();
                case "joinGame" -> joinGame();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (BadRequestException e) {
            return e.getMessage();
        }
    }

    public String register(String... params) throws BadRequestException {
        if (params.length == 3) {
            state=State.loggedIn;
            user=new UserData(params[0], params[1], params[2]);
            RegisterResult result=server.register
                    (new RegisterRequest(user.username(), user.password(), user.email()));
            auth=new AuthData(result.username(), result.authToken());
            //websocket stuff
            return String.format("Signed in as %s", result.username());
        }
        throw new BadRequestException("Expected: <username> <password> <email>");
    }

    public String logIn(String... params) throws BadRequestException {
        if (params.length == 2) {
            state=State.loggedIn;
            user=new UserData(params[0], params[1], null);
            LoginResult result=server.login(new LoginRequest(user.username(), user.password()));
            auth=new AuthData(result.username(), result.authToken());
            //websocket stuff
            return String.format("Signed in as %s", result.username());
        }
        throw new BadRequestException("Expected: <username> <password>");
    }

    public String logOut() throws BadRequestException {
        assertLoggedIn();
        state=State.loggedOut;
        server.logout();
        //websocket stuff
        return String.format("See you soon, %s!", user);
    }

    public String listGames() throws BadRequestException {
        assertLoggedIn();
        var games=server.listGames(new ListGamesRequest(auth.authToken())).games();
        var result=new StringBuilder();
        var gson=new Gson();
        for (var game : games) {
            result.append(gson.toJson(game)).append('\n');
        }
        return result.toString();
    }

    public String createGame(String... params) throws BadRequestException {
        assertLoggedIn();
        if (params.length == 1) {
            try {
                server.createGame(new CreateGameRequest(auth.authToken(), params[0]));
                return String.format("%s created.", params[0]);
            } catch (BadRequestException _) {
            }
        }
        throw new BadRequestException("Expected: <gameName>");
    }

    public String joinGame(String... params) throws BadRequestException {
        assertLoggedIn();
        if (params.length == 2) {
            state = State.inGame;
            ChessGame.TeamColor color=(params[1].equalsIgnoreCase("WHITE")) ?
                    ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
            server.joinGame(new JoinGameRequest(auth.authToken(), color, Integer.parseInt(params[0])));
            return String.format("You have joined game %s as %s", params[1], params[0].toUpperCase());
        }
        throw new BadRequestException("Expected: <gameID> [WHITE|BLACK]");
    }


    public String help() {
        if (state == State.loggedOut) {
            return """
                    Please choose one of the following options:
                    - register <username> <password> <email>
                    - logIn <username> <password>
                    - quit
                    - help
                    """;
        } else if (state == State.loggedIn) {
            return """
                    Please choose one of the following options:
                    - listGames
                    - createGame <gameName>
                    - joinGame <gameID> [WHITE|BLACK]
                    - observeGame <gameID>
                    - logOut
                    - quit
                    - help
                    """;
        }
        return inGame.help();
    }

    private void assertLoggedIn() throws BadRequestException {
        if (state == State.loggedOut) {
            throw new BadRequestException("You must sign in.");
        }
    }
}



