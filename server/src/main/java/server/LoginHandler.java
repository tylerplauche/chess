package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.LoginRequest;
import model.LoginResult;
import service.LoginService;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoginHandler implements Route {

    private final LoginService loginService;
    private final Gson gson;

    public LoginHandler(DataAccess dataAccess) {
        this.loginService = new LoginService(dataAccess);
        this.gson = new Gson();
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            LoginRequest loginRequest = gson.fromJson(req.body(), LoginRequest.class);

            if (loginRequest == null
                    || loginRequest.username() == null || loginRequest.username().isBlank()
                    || loginRequest.password() == null || loginRequest.password().isBlank()) {

                res.status(400);
                return gson.toJson(new ErrorMessage("Missing username or password"));
            }

            LoginResult result = loginService.login(loginRequest);
            if (result == null) {
                // Login failed (invalid credentials)
                res.status(401);
                return gson.toJson(new ErrorMessage("Invalid username or password"));
            }

            res.status(200);
            return gson.toJson(result);

        } catch (DataAccessException e) {
            // You can customize these messages/statuses based on your service's exceptions
            int status = switch (e.getMessage()) {
                case "bad request" -> 400;
                case "unauthorized" -> 401;
                default -> 500;
            };
            res.status(status);
            return gson.toJson(new ErrorMessage("Error: " + e.getMessage()));

        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorMessage("Internal server error"));
        }
    }

    private record ErrorMessage(String message) {}
}
