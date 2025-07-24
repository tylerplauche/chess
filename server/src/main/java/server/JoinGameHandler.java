// server/JoinGameHandler.java
package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.JoinGameRequest;
import service.JoinGameService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class JoinGameHandler implements Route {
    private final DataAccess data;
    private final Gson gson = new Gson();

    public JoinGameHandler(DataAccess data) {
        this.data = data;
    }

    public Object handle(Request req, Response res) {
        try {
            String auth = req.headers("Authorization");
            JoinGameRequest request = gson.fromJson(req.body(), JoinGameRequest.class);
            JoinGameService service = new JoinGameService(data);
            var result = service.joinGame(auth, request);
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
