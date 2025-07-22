package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.LoginRequest;
import model.LoginResult;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class LoginService {
    private final DataAccess data;

    public LoginService(DataAccess data) {
        this.data = data;
    }

    public LoginResult login(LoginRequest request) throws DataAccessException {
        if (request.username() == null || request.password() == null) {
            throw new DataAccessException("bad request");
        }

        UserData user = data.getUser(request.username());

        // Verify user exists and password hash matches
        if (user == null || !BCrypt.checkpw(request.password(), user.password())) {
            throw new DataAccessException("unauthorized");
        }

        String authToken = UUID.randomUUID().toString();
        data.insertAuth(new AuthData(authToken, user.username()));

        return new LoginResult(user.username(), authToken);
    }
}
