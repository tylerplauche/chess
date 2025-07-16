package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.LogoutService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class LogoutHandler implements Route {
    private final Gson gson = new Gson();
    private final LogoutService logoutService;

    public LogoutHandler(LogoutService logoutService) {
        this.logoutService = logoutService;
    }


    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");

            logoutService.logout(authToken);
            System.out.println("Auth header: " + authToken);
            res.status(200);
            return "{}";
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(Map.of("message", "Error: unauthorized"));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: internal server error"));
        }
    }
}
