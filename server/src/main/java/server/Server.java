package server;

import com.google.gson.Gson;
import dataaccess.*;
import model.*;
import server.*;
import spark.*;

import javax.xml.crypto.Data;

public class Server {

    private UserService userService;
    private GameService gameService;
    private final UserDAO users = new MemoryUserDAO();
    private final AuthDAO auths = new MemoryAuthDAO();
    private final GameDAO games = new MemoryGameDAO();


    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::clear);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private void exceptionHandler(ResponseException exception, Request request, Response response) {
        response.status(exception.StatusCode());
    }

    public Object clear(Request request, Response response) throws DataAccessException {
        auths.clear();
        users.clear();
        games.clear();
        return "";
    }

    public Object register(Request request, Response response) {
        try {
            var user = new Gson().fromJson(request.toString(), RegisterRequest.class); //The toString() portion here is probably wrong.
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
            var user = new Gson().fromJson(request.toString(), LoginRequest.class); //The toString() portion here is probably wrong.
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
            var user = new Gson().fromJson(request.toString(), LogoutRequest.class);
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

    public Object listGames(Request request, Response response) throws DataAccessException, ResponseException {
        try {
            var auth = new Gson().fromJson(request.toString(), ListGamesRequest.class);
            ListGamesResult listGamesResult = gameService.listGames(auth);
            response.status(200);
            return new Gson().toJson(listGamesResult);
        } catch (DataAccessException e) {
            response.status(500);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));

        } catch (UnauthorizedException e) {
            response.status(401);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));
        }
    }

    public Object createGame(Request request, Response response) {
        try {
            var game = new Gson().fromJson(request.toString(), CreateGameRequest.class);
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
            var game = new Gson().fromJson(request.toString(), JoinGameRequest.class);
            JoinGameResult joinGameResult = gameService.joinGame(game);
            response.status(200);
            return "";

        } catch (DataAccessException e) {
            response.status(500);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));

        } catch (UnauthorizedException e) {
            response.status(401);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));

        } catch (AlreadyTakenException e) {
            response.status(403);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));

        }
    }

}




