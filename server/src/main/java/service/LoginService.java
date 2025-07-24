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
        System.out.println("Login attempt:");
        System.out.println("Provided: " + request.password());
        System.out.println("Stored hash: " + user.password());

        boolean validtest = BCrypt.checkpw(request.password(), user.password());
        System.out.println("Password valid? " + validtest);


        boolean valid = BCrypt.checkpw(request.password(), user.password());
        if (!valid) {
            throw new DataAccessException("unauthorized");
        }

        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, user.username());
        db.insertAuth(auth);
        return new LoginResult(auth.username(), auth.authToken());
    }

}
