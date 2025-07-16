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
            if (e.getMessage().equals("unauthorized")) res.status(401);
            else if (e.getMessage().equals("already taken")) res.status(403);
            else res.status(400);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
