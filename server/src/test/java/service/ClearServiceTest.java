package service;

import dataaccess.MemoryDataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {
    private MemoryDataAccess dataAccess;
    private ClearService clearService;

    @BeforeEach
    public void setUp() throws DataAccessException {
        dataAccess = new MemoryDataAccess();
        clearService = new ClearService(dataAccess);

        // Add some test data
        dataAccess.insertUser(new UserData("user", "pass", "email"));
        dataAccess.insertAuth(new AuthData("token", "user"));
        dataAccess.insertGame(new GameData(0, null, null, "Game", null));
    }

    @Test
    public void testClearSuccess() throws DataAccessException {
        clearService.clear();

        assertNull(dataAccess.getUser("user"));
        assertNull(dataAccess.getAuth("token"));
        assertTrue(dataAccess.listGames().isEmpty());
    }

    @Test
    public void testClearOnEmptyDatabase() {
        dataAccess.clear(); // ensure empty

        assertDoesNotThrow(() -> clearService.clear());
    }
}
