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

    @Override
    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            if (authToken == null || authToken.isBlank()) {
                res.status(401);
                return gson.toJson(Map.of("message", "Unauthorized: Missing auth token"));
            }

            CreateGameRequest request = gson.fromJson(req.body(), CreateGameRequest.class);
            if (request.gameName() == null || request.gameName().isBlank()) {
                res.status(400);
                //return gson.toJson(Map.of("message", "Missing or blank game name"));
            }

            var result = gameService.createGame(authToken, request);
            res.status(200); // Created
            return gson.toJson(result);
        } catch (DataAccessException e) {
            return HandlerUtils.handleDataAccessException(res, e);
        }
    }
}
