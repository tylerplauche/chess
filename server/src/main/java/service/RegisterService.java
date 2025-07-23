package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.RegisterRequest;
import model.RegisterResult;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class RegisterService {
    private final DataAccess data;

    public RegisterService(DataAccess data) {
        this.data = data;
    }

    public RegisterResult register(RegisterRequest request) throws DataAccessException {
        if (request.username() == null || request.password() == null || request.email() == null) {
            throw new DataAccessException("Missing required registration fields");
        }

        if (data.getUser(request.username()) != null) {
            throw new DataAccessException("Username already taken");
        }


        String hashedPassword = BCrypt.hashpw(request.password(), BCrypt.gensalt());

        UserData user = new UserData(request.username(), hashedPassword, request.email());
        data.insertUser(user);

        String authToken = UUID.randomUUID().toString();
        data.insertAuth(new AuthData(authToken, request.username()));

        return new RegisterResult(request.username(), authToken);
    }
}
