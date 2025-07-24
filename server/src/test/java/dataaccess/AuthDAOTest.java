package dataaccess;

import dataaccess.sql.AuthTokenDAOSQL;
import dataaccess.sql.UserDAOSQL;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class AuthDAOTest {

    private AuthTokenDAO authDAO;
    private UserDAO userDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        authDAO = new AuthTokenDAOSQL();
        userDAO = new UserDAOSQL();
        // Clear auth tokens and users to start clean
        authDAO.clear();
        userDAO.clear();


        UserData user = new UserData("testUser", "password123", "test@example.com");
        userDAO.insertUser(user);
    }

    @Test
    public void insertAuthTokenSuccess() throws DataAccessException {
        AuthData token = new AuthData("abc123", "testUser");
        authDAO.insertToken(token);

        AuthData retrieved = authDAO.getToken("abc123");
        assertNotNull(retrieved);
        assertEquals("testUser", retrieved.username());
        assertEquals("abc123", retrieved.authToken());
    }

    @Test
    public void insertAuthTokenDuplicateFails() throws DataAccessException {
        AuthData token = new AuthData("abc123", "testUser");
        authDAO.insertToken(token);

        assertThrows(DataAccessException.class, () -> authDAO.insertToken(token));
    }

    @Test
    public void getAuthTokenNotFoundReturnsNull() throws DataAccessException {
        assertNull(authDAO.getToken("nonexistent"));
    }

    @Test
    public void deleteAuthTokenRemovesToken() throws DataAccessException {
        AuthData token = new AuthData("abc123", "testUser");
        authDAO.insertToken(token);
        authDAO.deleteToken("abc123");
        assertNull(authDAO.getToken("abc123"));
    }
    @Test
    public void deleteNonexistentTokenDoesNothing() throws DataAccessException {
        // Should not throw
        assertDoesNotThrow(() -> authDAO.deleteToken("ghostToken"));
    }

    @Test
    public void clearAuthTokensWorksOnEmptyTable() throws DataAccessException {
        // Clear again to ensure table is empty
        authDAO.clear();

        // Should not throw or fail
        assertDoesNotThrow(() -> authDAO.clear());
    }

}
