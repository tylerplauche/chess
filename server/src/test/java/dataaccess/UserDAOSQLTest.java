package dataaccess;

import dataaccess.sql.UserDAOSQL;
import model.UserData;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

public class UserDAOSQLTest {

    private UserDAOSQL userDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        userDAO = new UserDAOSQL();
        userDAO.clear();
    }

    @Test
    public void insertUserSuccess() throws DataAccessException {
        String rawPassword = "plaintextpassword";
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        UserData user = new UserData("testuser", hashedPassword, "test@example.com");
        userDAO.insertUser(user);

        UserData fetched = userDAO.getUser("testuser");
        assertNotNull(fetched);
        assertEquals("testuser", fetched.username());
        assertEquals("test@example.com", fetched.email());


        assertTrue(userDAO.verifyPassword("testuser", rawPassword));
    }
    @Test
    public void verifyPasswordFailsWithWrongPassword() throws DataAccessException {
        String rawPassword = "correctpassword";
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        UserData user = new UserData("wrongpassuser", hashedPassword, "test@example.com");
        userDAO.insertUser(user);

        assertFalse(userDAO.verifyPassword("wrongpassuser", "incorrectpassword"));
    }
    @Test
    public void verifyPasswordFailsWithNonexistentUser() throws DataAccessException {
        assertFalse(userDAO.verifyPassword("ghostuser", "anyPassword"));
    }

    @Test
    public void insertUserDuplicateThrowsException() throws DataAccessException {
        UserData user = new UserData("duplicateuser", "pass", "email@example.com");
        userDAO.insertUser(user);

        UserData duplicate = new UserData("duplicateuser", "pass2", "email2@example.com");
        assertThrows(DataAccessException.class, () -> {
            userDAO.insertUser(duplicate);
        });
    }

    @Test
    public void getUserNotFoundReturnsNull() throws DataAccessException {
        UserData user = userDAO.getUser("nonexistent");
        assertNull(user);
    }

    @Test
    public void clearRemovesAllUsers() throws DataAccessException {
        userDAO.insertUser(new UserData("user1", "pass1", "email1@example.com"));
        userDAO.insertUser(new UserData("user2", "pass2", "email2@example.com"));

        userDAO.clear();

        assertNull(userDAO.getUser("user1"));
        assertNull(userDAO.getUser("user2"));
    }
}
