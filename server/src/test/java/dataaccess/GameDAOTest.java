package dataaccess;

import chess.ChessGame;
import dataaccess.sql.GameDAOSQL;
import model.GameData;
import org.junit.jupiter.api.*;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class GameDAOTest {

    private GameDAO gameDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        gameDAO = new GameDAOSQL();
        gameDAO.clear();
    }

    @Test
    public void insertGameSuccess() throws DataAccessException {
        // gameID=1, no players yet, gameName="testGame"
        GameData game = new GameData(1, null,
                null, "testGame", new ChessGame());
        gameDAO.insertGame(game);

        GameData retrieved = gameDAO.getGame(1);
        assertNotNull(retrieved);
        assertEquals("testGame", retrieved.gameName());
    }

    @Test
    public void insertGameDuplicateIdFails() throws DataAccessException {
        GameData game = new GameData(1, null,
                null, "testGame", new ChessGame());
        gameDAO.insertGame(game);
        assertThrows(DataAccessException.class, () -> gameDAO.insertGame(game));
    }

    @Test
    public void getGameNotFoundReturnsNull() throws DataAccessException {
        assertNull(gameDAO.getGame(99));
    }

    @Test
    public void getAllGamesReturnsGames() throws DataAccessException {
        // Insert games with proper arguments (gameName is 4th param)
        gameDAO.insertGame(new GameData(1, null,
                null, "gameOne", new ChessGame()));
        gameDAO.insertGame(new GameData(2, null,
                null, "gameTwo", new ChessGame()));

        Collection<GameData> games = gameDAO.listGames();
        assertEquals(2, games.size());
    }

    @Test
    public void updateGameUpdatesPlayer() throws DataAccessException {
        GameData game = new GameData(1, null,
                null, "testGame", new ChessGame());
        gameDAO.insertGame(game);

        gameDAO.updateGame(1, "WHITE", "alice");

        GameData updated = gameDAO.getGame(1);
        assertEquals("alice", updated.whiteUsername());
    }
}
