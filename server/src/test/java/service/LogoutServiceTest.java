package service;

import dataaccess.MemoryDataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LogoutServiceTest {
    private MemoryDataAccess dataAccess;
    private LogoutService logoutService;

    @BeforeEach
    public void setUp() throws DataAccessException {
        dataAccess = new MemoryDataAccess();
        logoutService = new LogoutService(dataAccess);

        // Insert a known auth token
        dataAccess.insertAuth(new AuthData("valid-token", "user"));
    }

    @Test
    public void testLogoutSuccess() throws DataAccessException {
        logoutService.logout("valid-token");
        assertNull(dataAccess.getAuth("valid-token"));
    }

    @Test
    public void testLogoutInvalidToken() {
        assertThrows(DataAccessException.class, () -> logoutService.logout("invalid-token"));
    }
}
