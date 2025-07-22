package service;

import dataaccess.UserDAO;
import dataaccess.DataAccessException;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    private UserDAO userDAO;

    public UserService(UserDAO dao) {
        this.userDAO = dao;
    }

    public boolean login(String username, String password) throws DataAccessException {
        return userDAO.verifyPassword(username, password);
    }
}
