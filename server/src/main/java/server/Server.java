package server;

import com.google.gson.Gson;
import dataaccess.*;
import exceptions.*;
import model.*;
import requests.*;
import results.*;
import server.websocket.WebSocketHandler;
import spark.*;

public class Server {

    private final UserDAO users = new SQLUserDAO();
    private final AuthDAO auths = new SQLAuthDAO();
    private final GameDAO games = new SQLGameDAO();

    private final UserService userService = new UserService(users, auths);
    private final GameService gameService = new GameService(auths, games, users);

    private final WebSocketHandler webSocketHandler = new WebSocketHandler(userService, gameService);


    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Spark.webSocket("/ws", webSocketHandler);

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::clear);

        //This line initializes the server and can be removed once you have a functioning endpoint 


        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    //potentially should authenticate user before clear
    public Object clear(Request request, Response response) throws DataAccessException {
        auths.clear();
        users.clear();
        games.clear();
        response.status(200);
        return "";
    }

    public Object register(Request request, Response response) {
        try {
            RegisterRequest user = new Gson().fromJson(request.body(), RegisterRequest.class);
            RegisterResult registerResult = userService.register(user);
            response.status(200);
            return new Gson().toJson(registerResult);

        } catch (DataAccessException e) {
            response.status(500);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));

        } catch (BadRequestException e) {
            response.status(400);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));

        } catch (AlreadyTakenException e) {
            response.status(403);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));
        }
    }

    public Object login(Request request, Response response) {
        try {
            LoginRequest user = new Gson().fromJson(request.body(), LoginRequest.class); //The toString() portion here is probably wrong.
            LoginResult loginResult = userService.login(user);
            response.status(200);
            return new Gson().toJson(loginResult);
        } catch (DataAccessException e) {
            response.status(500);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));

        } catch (UnauthorizedException e) {
            response.status(401);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));
        }
    }

    public Object logout(Request request, Response response) {
        try {
            LogoutRequest user = new LogoutRequest(request.headers("authorization"));
            userService.logout(user);
            response.status(200);
            return "";

        } catch (DataAccessException e) {
            response.status(500);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));

        } catch (UnauthorizedException e) {
            response.status(401);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));
        }
    }

    public Object listGames(Request request, Response response) throws DataAccessException {
        try {
            String auth = request.headers("authorization");
            ListGamesResult listGamesResult = gameService.listGames(new ListGamesRequest(auth));
            response.status(200);
            return new Gson().toJson(listGamesResult);
        } catch (UnauthorizedException e) {
            response.status(401);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));
        }
    }

    public Object createGame(Request request, Response response) {
        try {
            GameData gameName = new Gson().fromJson(request.body(), GameData.class);
            String auth = request.headers("authorization");
            CreateGameRequest game = new CreateGameRequest(auth, gameName.gameName());
            CreateGameResult createGameResult = gameService.createGame(game);
            response.status(200);
            return new Gson().toJson(createGameResult);
        } catch (DataAccessException e) {
            response.status(500);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));

        } catch (UnauthorizedException e) {
            response.status(401);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));
        } catch (BadRequestException e) {
            response.status(400);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));
        }
    }

    public Object joinGame(Request request, Response response) {
        try {
            String auth = request.headers("authorization");
            JoinGameRequest game = new Gson().fromJson(request.body(), JoinGameRequest.class);
            game = new JoinGameRequest(auth, game.playerColor(), game.gameID());
            gameService.joinGame(game);
            response.status(200);
            return "";

        } catch (UnauthorizedException e) {
            response.status(401);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));
        } catch (BadRequestException e) {
            response.status(400);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));

        } catch (AlreadyTakenException e) {
            response.status(403);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));

        } catch (DataAccessException e) {
            response.status(500);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));
        }
    }

}
