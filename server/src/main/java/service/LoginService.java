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
    private final DataAccess db;

    public LoginService(DataAccess db) {
        this.db = db;
    }

    public LoginResult login(LoginRequest request) throws DataAccessException {
        if (request.username() == null || request.password() == null) {
            throw new DataAccessException("bad request");
        }

        UserData user = db.getUser(request.username());
        if (user == null) {
            throw new DataAccessException("unauthorized");
        }

        String stored = user.password();
        boolean match;

        try {
            match = BCrypt.checkpw(request.password(), stored);
        } catch (IllegalArgumentException e) {
            // stored password is not a bcrypt hash — compare raw text instead
            match = request.password().equals(stored);
        }

        if (!match) {
            throw new DataAccessException("unauthorized");
        }

        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, user.username());
        db.insertAuth(auth);
        return new LoginResult(auth.username(), auth.authToken());
    }
}
