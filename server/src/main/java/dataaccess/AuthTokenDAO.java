package dataaccess;

import model.AuthData;

public interface AuthTokenDAO {

    void insertToken(AuthData token) throws DataAccessException;

    AuthData getToken(String token) throws DataAccessException;

    void deleteToken(String token) throws DataAccessException;

    void clear() throws DataAccessException;
}
