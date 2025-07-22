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
    public void insertAuthTokensuccess() throws DataAccessException {
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
}
