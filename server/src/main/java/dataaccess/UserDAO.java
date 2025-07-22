package dataaccess;


import model.UserData;

public interface UserDAO {

    void insertUser(UserData user) throws DataAccessException;

    UserData getUser(String username) throws DataAccessException;

    void clear() throws DataAccessException;
}
