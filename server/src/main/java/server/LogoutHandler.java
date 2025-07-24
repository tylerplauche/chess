
package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import service.LogoutService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class LogoutHandler implements Route {
    private final LogoutService logoutService;
    private final Gson gson = new Gson();

    public LogoutHandler(LogoutService logoutService) {
        this.logoutService = logoutService;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            if (authToken == null || authToken.isBlank()) {
                res.status(401);
                //return gson.toJson(Map.of("message", "Unauthorized: Missing auth token"));
            }

            logoutService.logout(authToken);
            res.status(200);
            return gson.toJson(Map.of("message", "Logout successful"));
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