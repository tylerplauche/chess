package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.RegisterRequest;
import service.RegisterService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class RegisterHandler implements Route {
    private final Gson gson = new Gson();
    private final RegisterService registerService;

    public RegisterHandler(DataAccess db) {
        this.registerService = new RegisterService(db); // Use shared db
    }

    public Object handle(Request req, Response res) {
        try {
            RegisterRequest request = gson.fromJson(req.body(), RegisterRequest.class);
            var result = registerService.register(request);
            res.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            res.status(e.getMessage().contains("already taken") ? 403 : 400);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
