package ui;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;

public class ServerFacade {
    private final String serverUrl;
    private final HttpClient client = HttpClient.newHttpClient();

    private final Gson gson = new Gson();

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public AuthData register(String username, String password, String email) throws Exception {
        RegisterRequest req = new RegisterRequest(username, password, email);
        RegisterResult res = makeRequest("POST", "/user", req, null, RegisterResult.class);
        return new AuthData(res.authToken(), res.username());
    }

    public AuthData login(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest(username, password);
        LoginResult res = makeRequest("POST", "/session", req, null, LoginResult.class);
        return new AuthData(res.authToken(), res.username());
    }

    public void logout(String authToken) throws Exception {
        makeRequest("DELETE", "/session", null, authToken, null);
    }


    public GameData createGame(String authToken, String gameName) throws Exception {
        CreateGameRequest req = new CreateGameRequest(gameName);
        CreateGameResult res = makeRequest("POST", "/game", req, authToken, CreateGameResult.class);
        return new GameData(res.gameID(), null, null, gameName, new ChessGame()); // you can adjust white/black usernames later
    }

    public Collection<GameData> listGames(String authToken) throws Exception {
        ListGamesResult res = makeRequest("GET", "/game", null, authToken, ListGamesResult.class);
        return res.games();
    }


    public void joinGame(String authToken, int gameID, String playerColor) throws Exception {
        JoinGameRequest req = new JoinGameRequest(playerColor, gameID);
        makeRequest("PUT", "/game", req, authToken, null);
    }


    public void clear() throws Exception {
        makeRequest("DELETE", "/db", null, null, null);
    }





    private <T> T makeRequest(String method, String path, Object body, String authToken, Class<T> responseType) throws Exception {
        URL url = new URL(serverUrl + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Accept", "application/json");
        if (authToken != null) {
            connection.setRequestProperty("Authorization", authToken);
        }

        if (body != null) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            try (OutputStream os = connection.getOutputStream()) {
                String json = gson.toJson(body);
                os.write(json.getBytes());
            }
        }

        int status = connection.getResponseCode();
        if (status / 100 != 2) {
            InputStream errorStream = connection.getErrorStream();
            String error = new String(errorStream.readAllBytes());
            throw new Exception("Error " + status + ": " + error);
        }

        if (responseType == null) return null;

        try (InputStream is = connection.getInputStream();
             InputStreamReader reader = new InputStreamReader(is)) {
            return gson.fromJson(reader, responseType);
        }
    }
}
