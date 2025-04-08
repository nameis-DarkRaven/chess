package client;

import chess.*;
import com.google.gson.Gson;
import exceptions.BadRequestException;
import model.*;
import requests.*;
import results.*;
import serverfacade.ServerFacade;
import client.websocket.*;
import websocket.messages.*;

import java.util.Arrays;

import static client.EscapeSequences.SET_TEXT_COLOR_RED;

public class Client implements NotificationHandler{
//public class Client {
    private final ServerFacade server;
    private final String serverURL;
    private State state;
    private UserData user;
    private AuthData auth;
    private GameData game;
    private GameClient inGame;
    private WebSocketFacade ws;

        public Client(String serverURL) {
        state = State.loggedOut;
        server = new ServerFacade(serverURL);
        this.serverURL = serverURL;

    }

    public String eval(String input) {
        try {
            var in = input.strip();
            var tokens = in.split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd.toLowerCase()) {
                case "register" -> register(params);
                case "login" -> logIn(params);
                case "logout" -> logOut();
                case "games" -> listGames();
                case "create" -> createGame(params);
                case "join" -> joinGame(params);
                case "redraw" -> redraw();
                case "observe" -> observe(params);
                case "leave" -> leave();
                case "quit" -> "quit";
                case "clear" -> clear(params);
                default -> help();
            };
        } catch (BadRequestException e) {
            return e.getMessage();
        }
    }

    private String clear(String... params) throws BadRequestException {
        try {
            if (params.length != 1) {
                throw new BadRequestException("Unauthorized.");
            }
            if (params[0].equals("biscuit")) {
                server.clear();
                return "Cleared.";
            }
            throw new BadRequestException("Unauthorized.");
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    public String leave() {
        state = State.loggedIn;
//        String gameName = game.gameName();
        game = null;
//        return String.format("Left %s", gameName);
        return "Left game.";
    }

    public String register(String... params) throws BadRequestException {
        if (params.length == 3) {
            try {
                assertNotEmpty(params);
            } catch (BadRequestException e) {
                throw new BadRequestException("Expected: <username> <password> <email>");
            }
            try {
                assertLoggedOut();
                user = new UserData(params[0], params[1], params[2]);
                RegisterResult result = server.register
                        (new RegisterRequest(user.username(), user.password(), user.email()));
                auth = new AuthData(result.username(), result.authToken());
                state = State.loggedIn;
                //websocket stuff
                return String.format("Signed in as %s.", result.username());
            } catch (BadRequestException e) {
                throw new BadRequestException("Username already taken.");
            }
        }
        throw new BadRequestException("Expected: <username> <password> <email>");
    }

    public String logIn(String... params) throws BadRequestException {
        if (params.length == 2) {
            try {
                assertNotEmpty(params);
            } catch (BadRequestException e) {
                throw new BadRequestException("Expected: <username> <password>");
            }
            assertLoggedOut();
            user = new UserData(params[0], params[1], null);
            LoginResult result = server.login(new LoginRequest(user.username(), user.password()));
            auth = new AuthData(result.username(), result.authToken());
            state = State.loggedIn;
            //websocket stuff
            return String.format("Signed in as %s.", result.username());
        }
        throw new BadRequestException("Expected: <username> <password>");
    }

    public String logOut() throws BadRequestException {
        if (state == State.inGame) {
            throw new BadRequestException("You must leave the game first.");
        }
        assertLoggedIn();
        server.logout(new LogoutRequest(auth.authToken()));
        state = State.loggedOut;
        auth = null;
        //websocket stuff
        return String.format("See you soon, %s!", user.username());
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
        try {
            assertNotEmpty(params);
        } catch (BadRequestException e) {
            throw new BadRequestException("Expected: <gameName>");
        }
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
        if (state.equals(State.inGame)) {
            try {
                assertLoggedIn();
            } catch (BadRequestException e) {
                throw new BadRequestException("You must leave the game first.");
            }
        }
        assertLoggedIn();
        if (params.length == 2) {
            try {
                assertNotEmpty(params);
            } catch (BadRequestException e) {
                throw new BadRequestException("Expected: <gameID> [WHITE|BLACK]");
            }
            try {
                if (params[1].equalsIgnoreCase("white") || params[1].equalsIgnoreCase("black")) {
                    ChessGame.TeamColor color = (params[1].equalsIgnoreCase("WHITE")) ?
                            ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
                    server.joinGame(new JoinGameRequest(auth.authToken(), color, Integer.parseInt(params[0])));
                    inGame = new GameClient();
                    state = State.inGame;
                    System.out.printf("You have joined game %s as %s\n", params[0], params[1].toUpperCase());
                    inGame.printBoard(color);
                    return "";
                }
            } catch (BadRequestException e) {
                throw new BadRequestException("Spot already taken.");
            }
        }
        throw new BadRequestException("Expected: <gameID> [WHITE|BLACK]");
    }

    private String observe(String... params) throws BadRequestException {
        assertLoggedIn();
        if (params.length == 1) {
            try {
                assertNotEmpty(params);
            } catch (BadRequestException e) {
                throw new BadRequestException("Expected: <gameID>");
            }
            inGame = new GameClient();
            state = State.observing;
            inGame.printBoard(ChessGame.TeamColor.WHITE);
            return String.format("You have joined game %s as an observer.\n", params[0].toUpperCase());
        }
        throw new BadRequestException("Expected <gameID>");
    }

    private String redraw() throws BadRequestException {
        assertInGame();
        if (user.username().equals(game.blackUsername())) {
            inGame.printBoard(ChessGame.TeamColor.BLACK);
        } else {
            inGame.printBoard(ChessGame.TeamColor.WHITE);
        }
        return "";
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
        } else if (state == State.inGame) {
            return """
                    Please choose one of the following options:
                    - highlight <position> (ex. f5)
                    - move <source> <destination> <optional: promotion> (ex. f5 e4 queen)
                    - redraw
                    - resign
                    - leave
                    - help
                    """;
        } else {
            return """
                    - highlight <position> (ex. f5)
                    - redraw
                    - leave
                    - help
                    """;
        }
    }


    private void assertLoggedIn() throws BadRequestException {
        if (state != State.loggedIn) {
            throw new BadRequestException("You must log in first.");
        }
    }

    private void assertLoggedOut() throws BadRequestException {
        if (state != State.loggedOut) {
            throw new BadRequestException("You are already logged in.");
        }
    }

    private void assertInGame() throws BadRequestException {
        if (state != State.inGame) {
            throw new BadRequestException("You must join a game first.");
        }
    }

    private void assertNotEmpty(String... params) throws BadRequestException {
        for (String param : params) {
            if (param.isEmpty() || param.equals(" ")) {
                throw new BadRequestException("Unexpected input.");
            }
        }
    }

    @Override
    public void notify(ServerMessage notification) {
//        System.out.println(SET_TEXT_COLOR_RED + notification.message()); //???
    }
}



