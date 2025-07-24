// server/ListGamesHandler.java
package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import service.ListGamesService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class ListGamesHandler implements Route {
    private final Gson gson = new Gson();
    private final DataAccess db;

    public ListGamesHandler(DataAccess db) {
        this.db = db;
    }

    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            ListGamesService service = new ListGamesService(db);
            var result = service.listGames(authToken);

            res.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            String message = e.getMessage().toLowerCase();

            if (message.contains("already taken")) {
                res.status(403); // Forbidden
            } else if (message.contains("unauthorized") || message.contains("invalid credentials")) {
                res.status(401); // Unauthorized
            } else if (message.contains("connection")) {
                res.status(500); // Server error
            } else {
                res.status(400); // Bad request
            }

            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
