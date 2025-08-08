package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import service.LeaveGameService;
import spark.Request;
import spark.Response;
import spark.Route;

public class LeaveGameHandler implements Route {
    private final LeaveGameService service;

    public LeaveGameHandler(DataAccess db) {
        this.service = new LeaveGameService(db);
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            int gameID = Integer.parseInt(req.params(":gameID")); // Use "/game/:gameID/leave" route

            service.leaveGame(authToken, gameID);
            res.status(200);
            return "{}";
        } catch (DataAccessException e) {
            res.status(403);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return new Gson().toJson(new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private record ErrorMessage(String message) {}
}
