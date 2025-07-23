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
    private final Gson serializer;

    public LoginHandler(DataAccess dataAccess) {
        this.loginService = new LoginService(dataAccess);
        this.serializer = new Gson();
    }

    public Object handle(Request request, Response response) {
        LoginRequest loginRequest = serializer.fromJson(request.body(), LoginRequest.class);

        try {
            LoginResult result = loginService.login(loginRequest);
            response.status(200);
            return serializer.toJson(result);

        } catch (DataAccessException exception) {
            response.status(determineStatusCode(exception));
            return serializer.toJson(new ErrorMessage("Error: " + exception.getMessage()));
        }
    }

    private int determineStatusCode(DataAccessException exception) {
        String message = exception.getMessage();
        if ("bad request".equals(message)) return 400;
        if ("unauthorized".equals(message)) return 401;
        return 500;
    }

    private record ErrorMessage(String message) {}
}