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
            return HandlerUtils.handleDataAccessException(res, e);
        }
    }
}
