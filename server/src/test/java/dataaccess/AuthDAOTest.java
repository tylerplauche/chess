package dataaccess;

import dataaccess.sql.AuthTokenDAOSQL;
import model.AuthData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class AuthDAOTest {

    private AuthTokenDAO authDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        authDAO = new AuthTokenDAOSQL();
        authDAO.clear();
    }

    @Test
    public void insertAuthToken_success() throws DataAccessException {
        AuthData token = new AuthData("abc123", "testUser");
        authDAO.insertToken(token);

        AuthData retrieved = authDAO.getToken("abc123");
        assertNotNull(retrieved);
        assertEquals("testUser", retrieved.username());
        assertEquals("abc123", retrieved.authToken());
    }

    @Test
    public void insertAuthToken_duplicate_fails() throws DataAccessException {
        AuthData token = new AuthData("abc123", "testUser");
        authDAO.insertToken(token);
        assertThrows(DataAccessException.class, () -> authDAO.insertToken(token));
    }

    @Test
    public void getAuthToken_notFound_returnsNull() throws DataAccessException {
        assertNull(authDAO.getToken("nonexistent"));
    }

    @Test
    public void deleteAuthToken_removesToken() throws DataAccessException {
        AuthData token = new AuthData("abc123", "testUser");
        authDAO.insertToken(token);
        authDAO.deleteToken("abc123");
        assertNull(authDAO.getToken("abc123"));
    }
}
