package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.CreateGameRequest;
import model.CreateGameResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {
    private DataAccess db;
    private GameService gameService;
    private final String validAuthToken = "valid-token";
    private final String username = "testUser";

    @BeforeEach
    public void setUp() throws DataAccessException {
        db = new MemoryDataAccess();
        gameService = new GameService(db);
        db.insertAuth(new AuthData(validAuthToken, username));
    }

    @Test
    public void createGameSuccess() throws DataAccessException {
        CreateGameRequest request = new CreateGameRequest("My Test Game");
        CreateGameResult result = gameService.createGame(validAuthToken, request);

        assertNotNull(result);
        assertTrue(result.gameID() > 0);
    }

    @Test
    public void createGameUnauthorized() {
        CreateGameRequest request = new CreateGameRequest("Another Game");
        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> gameService.createGame("invalid-token", request));
        assertEquals("unauthorized", exception.getMessage());
    }

    @Test
    public void createGameBadRequest() {
        CreateGameRequest request = new CreateGameRequest("");
        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> gameService.createGame(validAuthToken, request));
        assertEquals("bad request", exception.getMessage());
    }
}
