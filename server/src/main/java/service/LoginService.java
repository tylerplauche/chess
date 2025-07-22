package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.LoginRequest;
import model.LoginResult;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class LoginService {

    private final DataAccess data;

    public LoginService(DataAccess data) {
        this.data = data;
    }

    public LoginResult login(LoginRequest request) throws DataAccessException {
        if (request == null
                || request.username() == null || request.username().isBlank()
                || request.password() == null || request.password().isBlank()) {
            throw new DataAccessException("bad request");
        }

        // Get the user data from DB
        var user = data.getUser(request.username());
        if (user == null) {
            throw new DataAccessException("unauthorized");
        }

        // Check password using BCrypt
        if (!BCrypt.checkpw(request.password(), user.password())) {
            throw new DataAccessException("unauthorized");
        }

        // Generate new auth token
        String authToken = UUID.randomUUID().toString();

        // Store auth token linked to username
        data.insertAuth(new AuthData(authToken, user.username()));

        return new LoginResult(user.username(), authToken);
    }
}
