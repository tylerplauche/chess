package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.GameData;
import model.JoinGameRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JoinGameServiceTest {

    private MemoryDataAccess dataAccess;
    private JoinGameService service;

    @BeforeEach
    public void setup() throws DataAccessException {
        dataAccess = new MemoryDataAccess();


        service = new JoinGameService(dataAccess);

        // Add a user and auth token
        dataAccess.insertUser(new model.UserData("alice", "pass123", "alice@example.com"));
        dataAccess.insertAuth(new AuthData("token123", "alice"));

        // Create a game with empty slots
        GameData game = new GameData(1, null, null, "Chess Match", null);
        dataAccess.insertGame(game);
    }

    @Test
    public void joinGameSuccessWhite() throws DataAccessException {
        JoinGameRequest request = new JoinGameRequest("white", 1);
        var result = service.joinGame("token123", request);

        GameData updatedGame = dataAccess.getGame(1);
        assertEquals("alice", updatedGame.whiteUsername());
        assertNull(updatedGame.blackUsername());
        assertNotNull(result);
    }

    @Test
    public void joinGameSuccessBlack() throws DataAccessException {
        // First join white
        service.joinGame("token123", new JoinGameRequest("white", 1));

        // Insert another user & auth
        dataAccess.insertUser(new model.UserData("bob", "password", "bob@example.com"));
        dataAccess.insertAuth(new AuthData("token456", "bob"));

        JoinGameRequest request = new JoinGameRequest("black", 1);
        var result = service.joinGame("token456", request);

        GameData updatedGame = dataAccess.getGame(1);
        assertEquals("alice", updatedGame.whiteUsername());
        assertEquals("bob", updatedGame.blackUsername());
        assertNotNull(result);
    }

    @Test
    public void joinGameAlreadyTakenThrows() throws DataAccessException {
        service.joinGame("token123", new JoinGameRequest("white", 1));

        // Try joining white again with a different user
        dataAccess.insertUser(new model.UserData("bob", "password", "bob@example.com"));
        dataAccess.insertAuth(new AuthData("token456", "bob"));

        JoinGameRequest request = new JoinGameRequest("white", 1);
        DataAccessException ex = assertThrows(DataAccessException.class, () ->
                service.joinGame("token456", request)
        );
        assertEquals("already taken", ex.getMessage());
    }

    @Test
    public void joinGameUnauthorizedThrows() {
        JoinGameRequest request = new JoinGameRequest("white", 1);

        DataAccessException ex = assertThrows(DataAccessException.class, () ->
                service.joinGame("badtoken", request)
        );
        assertEquals("unauthorized", ex.getMessage());
    }

    @Test
    public void joinGameBadRequestThrows() {
        // Null auth token
        assertThrows(DataAccessException.class, () -> service.joinGame(null, new JoinGameRequest("white", 1)));

        // Null color
        //assertThrows(DataAccessException.class, () -> service.joinGame("token123", new JoinGameRequest(null, 1)));

        // Null game ID
        assertThrows(DataAccessException.class, () -> service.joinGame("token123", new JoinGameRequest("white", null)));

        // Invalid color
        assertThrows(DataAccessException.class, () -> service.joinGame("token123", new JoinGameRequest("green", 1)));

        // Game doesn't exist
        assertThrows(DataAccessException.class, () -> service.joinGame("token123", new JoinGameRequest("white", 999)));
    }
}
