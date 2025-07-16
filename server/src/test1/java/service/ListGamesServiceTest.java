// test/service/ListGamesServiceTest.java
package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.GameData;
import model.ListGamesResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class ListGamesServiceTest {
    private MemoryDataAccess db;
    private ListGamesService service;
    private String validAuthToken;

    @BeforeEach
    public void setUp() throws DataAccessException {
        db = new MemoryDataAccess();
        service = new ListGamesService(db);

        // Set up valid auth
        String username = "testuser";
        validAuthToken = "token123";
        db.insertAuth(new AuthData(validAuthToken, username));

        // Insert games
        db.insertGame(new GameData(0, null, null, "Game One", null));
        db.insertGame(new GameData(0, null, null, "Game Two", null));
    }

    @Test
    public void listGames_validAuth_returnsGames() throws DataAccessException {
        ListGamesResult result = service.listGames(validAuthToken);
        Collection<GameData> games = result.games();
        assertNotNull(games);
        assertEquals(2, games.size());
    }

    @Test
    public void listGames_invalidAuth_throwsException() {
        var ex = assertThrows(DataAccessException.class, () -> service.listGames("bad-token"));
        assertEquals("unauthorized", ex.getMessage());
    }

    @Test
    public void listGames_nullAuth_throwsException() {
        var ex = assertThrows(DataAccessException.class, () -> service.listGames(null));
        assertEquals("unauthorized", ex.getMessage());
    }
}
