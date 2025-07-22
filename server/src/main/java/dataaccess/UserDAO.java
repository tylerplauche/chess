package dataaccess;


import model.UserData;

public interface UserDAO {

    void insertUser(UserData user) throws DataAccessException;

    UserData getUser(String username) throws DataAccessException;

    boolean verifyPassword(String username, String providedPassword) throws DataAccessException;


    void clear() throws DataAccessException;
}
