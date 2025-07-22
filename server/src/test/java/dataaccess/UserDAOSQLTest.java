package dataaccess;

import dataaccess.sql.UserDAOSQL;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserDAOSQLTest {

    private UserDAOSQL userDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        // Initialize DAO
        userDAO = new UserDAOSQL();
        // Clear users table before each test
        userDAO.clear();
    }

    @Test
    public void insertUser_Success() throws DataAccessException {
        UserData user = new UserData("testuser", "hashedpassword", "test@example.com");
        userDAO.insertUser(user);

        UserData fetched = userDAO.getUser("testuser");
        assertNotNull(fetched);
        assertEquals("testuser", fetched.username());
        assertEquals("hashedpassword", fetched.passwordHash());
        assertEquals("test@example.com", fetched.email());
    }

    @Test
    public void insertUser_Duplicate_ThrowsException() throws DataAccessException {
        UserData user = new UserData("duplicateuser", "pass", "email@example.com");
        userDAO.insertUser(user);

        UserData duplicate = new UserData("duplicateuser", "pass2", "email2@example.com");
        assertThrows(DataAccessException.class, () -> {
            userDAO.insertUser(duplicate);
        });
    }

    @Test
    public void getUser_NotFound_ReturnsNull() throws DataAccessException {
        UserData user = userDAO.getUser("nonexistent");
        assertNull(user);
    }

    @Test
    public void clear_RemovesAllUsers() throws DataAccessException {
        userDAO.insertUser(new UserData("user1", "pass1", "email1@example.com"));
        userDAO.insertUser(new UserData("user2", "pass2", "email2@example.com"));

        userDAO.clear();

        assertNull(userDAO.getUser("user1"));
        assertNull(userDAO.getUser("user2"));
    }
}
