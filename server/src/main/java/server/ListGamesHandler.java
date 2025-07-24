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
            return HandlerUtils.handleDataAccessException(res, e);
        }
    }
}
