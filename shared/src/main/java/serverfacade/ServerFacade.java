package serverfacade;

import com.google.gson.Gson;
import requests.*;
import results.*;
import exceptions.*;

import java.io.*;
import java.net.*;


public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl=url;
    }

    public RegisterResult register(RegisterRequest request)
            throws BadRequestException {
        var path="/user";
        return this.makeRequest("POST", path, request, RegisterResult.class);
    }

    public LoginResult login(LoginRequest request) throws BadRequestException {
        var path="/session";
        return this.makeRequest("POST", path, request, LoginResult.class);
    }

    public LogoutResult logout() throws BadRequestException {
        var path="/session";
        return this.makeRequest("DELETE", path, null, null);
    }

    public ListGamesResult listGames(ListGamesRequest request) throws BadRequestException {
        var path="/game";
        return this.makeRequest("GET", path, request, ListGamesResult.class);
    }

    public CreateGameResult createGame(CreateGameRequest request) throws BadRequestException {
        var path="/game";
        return this.makeRequest("POST", path, request, CreateGameResult.class);
    }

    public JoinGameResult joinGame(JoinGameRequest request) throws BadRequestException {
        var path="/game";
        return this.makeRequest("PUT", path, request, JoinGameResult.class);
    }

    public void clear() throws BadRequestException {
        var path="/db";
        this.makeRequest("DELETE", path, null, null);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws BadRequestException {
        try {
            URL url=(new URI(serverUrl + path)).toURL();
            HttpURLConnection http=(HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);


            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }


    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData=new Gson().toJson(request);
            try (OutputStream reqBody=http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, BadRequestException {
        int status=http.getResponseCode();
        if (status / 100 == 2) {
            try (InputStream err=http.getErrorStream()) {
                if (err != null) {
                    throw BadRequestException.fromJson(err);
                }
            }
            throw new BadRequestException("other failure: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response=null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody=http.getInputStream()) {
                InputStreamReader reader=new InputStreamReader(respBody);
                if (responseClass != null) {
                    response=new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }
}