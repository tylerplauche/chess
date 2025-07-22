package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;

public class LogoutService {
    private final DataAccess data;

    public LogoutService(DataAccess data) {
        this.data = data;
    }

    public void logout(String authToken) throws DataAccessException {
        if (authToken == null || authToken.isEmpty()) {
            throw new DataAccessException("unauthorized");
        }
        if (data.getAuth(authToken) == null) {
            throw new DataAccessException("unauthorized");
        }
        data.deleteAuth(authToken);
    }
}
