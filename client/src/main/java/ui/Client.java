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
    private State state = State.loggedOut;
    private UserData user;
    private AuthData auth;
    private InGameClient inGame;

    public Client(String serverURL) {
        server = new ServerFacade(serverURL);
        this.serverURL = serverURL;
    }

    public String eval(String input) {
        try {
            var in = input.toLowerCase().strip();
            var tokens = in.split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> logIn(params);
                case "logout" -> logOut();
                case "games" -> listGames();
                case "create" -> createGame(params);
                case "join" -> joinGame(params);
                case "quit" -> "quit";
                case "clear" -> clear();
                default -> help();
            };
        } catch (BadRequestException e) {
            return e.getMessage();
        }
    }

    public String clear() throws BadRequestException {
        try {
            server.clear();
            return "Cleared.";
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    public String register(String... params) throws BadRequestException {
        try {
            if (params.length == 3) {
                state = State.loggedIn;
                user = new UserData(params[0], params[1], params[2]);
                RegisterResult result = server.register
                        (new RegisterRequest(user.username(), user.password(), user.email()));
                auth = new AuthData(result.username(), result.authToken());
                //websocket stuff
                return String.format("Signed in as %s.", result.username());
            }
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
        throw new BadRequestException("Expected: <username> <password> <email>");
    }

    public String logIn(String... params) throws BadRequestException {
        if (params.length == 2) {
            state = State.loggedIn;
            user = new UserData(params[0], params[1], null);
            LoginResult result = server.login(new LoginRequest(user.username(), user.password()));
            auth = new AuthData(result.username(), result.authToken());
            //websocket stuff
            return String.format("Signed in as %s.", result.username());
        }
        throw new BadRequestException("Expected: <username> <password>");
    }

    public String logOut() throws BadRequestException {
        assertLoggedIn();
        state = State.loggedOut;
        server.logout(new LogoutRequest(auth.authToken()));
        //websocket stuff
        return String.format("See you soon, %s!", user);
    }

    public String listGames() throws BadRequestException {
        assertLoggedIn();
        try {
            var games = server.listGames(new ListGamesRequest(auth.authToken())).games();
            var result = new StringBuilder();
            var gson = new Gson();
            int i = 0;
            for (var game : games) {
                i++;
                result.append(i).append(".  ");
                result.append("Game name:").append(gson.toJson(game.gameName())).append("  ");
                result.append("White: ").append(gson.toJson(game.whiteUsername())).append("  ");
                result.append("Black: ").append(gson.toJson(game.blackUsername())).append('\n');
            }
            return result.toString();
        } catch (BadRequestException e) {
            throw new BadRequestException("No games yet. Create one.");
        }
    }

    public String createGame(String... params) throws BadRequestException {
        assertLoggedIn();
        if (params.length == 1) {
            try {
                server.createGame(new CreateGameRequest(auth.authToken(), params[0]));
                return String.format("%s created.", params[0]);
            } catch (BadRequestException e) {
                throw new BadRequestException(e.getMessage());
            }
        }
        throw new BadRequestException("Expected: <gameName>");
    }

    public String joinGame(String... params) throws BadRequestException {
        assertLoggedIn();
        if (params.length == 2) {
            state = State.inGame;
            inGame = new InGameClient();
            ChessGame.TeamColor color = (params[1].equalsIgnoreCase("WHITE")) ?
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
                    - login <username> <password>
                    - quit
                    - help
                    """;
        } else if (state == State.loggedIn) {
            return """
                    Please choose one of the following options:
                    - games
                    - create <gameName>
                    - join <gameID> [WHITE|BLACK]
                    - observe <gameID>
                    - logout
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



