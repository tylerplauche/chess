package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.RegisterRequest;
import model.RegisterResult;
import model.UserData;

import java.util.UUID;

public class RegisterService {
    private final DataAccess data;

    public RegisterService(DataAccess data) {
        this.data = data;
    }

    public RegisterResult register(RegisterRequest request) throws DataAccessException {
        if (request.username() == null || request.password() == null || request.email() == null) {
            throw new DataAccessException("bad request");
        }
        if (data.getUser(request.username()) != null) {
            throw new DataAccessException("already taken");
        }

        UserData user = new UserData(request.username(), request.password(), request.email());
        data.insertUser(user);

        String authToken = UUID.randomUUID().toString();
        data.insertAuth(new AuthData(authToken, request.username()));

        return new RegisterResult(request.username(), authToken);
    }
}
