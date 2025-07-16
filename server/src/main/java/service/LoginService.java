package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.LoginRequest;
import model.LoginResult;
import model.UserData;

import java.util.UUID;

public class LoginService {
    private final DataAccess data;

    public LoginService(DataAccess data) {
        this.data = data;
    }

    public LoginResult login(LoginRequest request) throws DataAccessException {
        // Validate input
        if (request.username() == null || request.password() == null) {
            throw new DataAccessException("bad request");
        }

        // Fetch user by username
        UserData user = data.getUser(request.username());

        // Check user exists and password matches
        if (user == null || !user.password().equals(request.password())) {
            throw new DataAccessException("unauthorized");
        }

        // Generate new auth token and save it
        String authToken = UUID.randomUUID().toString();
        data.insertAuth(new AuthData(authToken, user.username()));

        // Return successful login result
        return new LoginResult(user.username(), authToken);
    }
}
