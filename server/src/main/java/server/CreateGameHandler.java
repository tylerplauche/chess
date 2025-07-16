package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.CreateGameRequest;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class CreateGameHandler implements Route {
    private final Gson gson = new Gson();
    private final GameService gameService;

    public CreateGameHandler(DataAccess db) {
        this.gameService = new GameService(db);
    }

    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            CreateGameRequest request = gson.fromJson(req.body(), CreateGameRequest.class);
            var result = gameService.createGame(authToken, request);
            res.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            res.status(e.getMessage().equals("unauthorized") ? 401 : 400);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
