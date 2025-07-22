package dataaccess;

import dataaccess.sql.UserDAOSQL;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class AuthTokenDAOSQLTest {
    private AuthTokenDAOSQL dao;

    @BeforeEach
    public void setup() throws DataAccessException {
        dao = new AuthTokenDAOSQL();
        dao.clear();
    }

    @Test
    public void insertToken_success() throws DataAccessException {
        AuthData token = new AuthData("abc123", "user1");
        dao.insertToken(token);

        AuthData found = dao.getToken("abc123");
        assertNotNull(found);
        assertEquals("user1", found.username());
    }

    @Test
    public void getToken_notFound() throws DataAccessException {
        assertNull(dao.getToken("not-a-token"));
    }
}
