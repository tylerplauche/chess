package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import spark.Response;

import java.util.Map;

public class HandlerUtils {
    private static final Gson GSON = new Gson();

    public static String handleDataAccessException(Response res, DataAccessException e) {
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

        return GSON.toJson(Map.of("message", "Error: " + e.getMessage()));
    }
}
