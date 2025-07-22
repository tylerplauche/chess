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
    public void insertGame_success() throws DataAccessException {
        GameData game = new GameData(1, "testGame", null, null, new ChessGame());
        gameDAO.insertGame(game);

        GameData retrieved = gameDAO.getGame(1);
        assertNotNull(retrieved);
        assertEquals("testGame", retrieved.gameName());
    }

    @Test
    public void insertGame_duplicateId_fails() throws DataAccessException {
        GameData game = new GameData(1, "testGame", null, null, new ChessGame());
        gameDAO.insertGame(game);
        assertThrows(DataAccessException.class, () -> gameDAO.insertGame(game));
    }

    @Test
    public void getGame_notFound_returnsNull() throws DataAccessException {
        assertNull(gameDAO.getGame(99));
    }

    @Test
    public void getAllGames_returnsGames() throws DataAccessException {
        gameDAO.insertGame(new GameData(1, "gameOne", null, null, new ChessGame()));
        gameDAO.insertGame(new GameData(2, "gameTwo", null, null, new ChessGame()));

        Collection<GameData> games = gameDAO.getAllGames();
        assertEquals(2, games.size());
    }

    @Test
    public void updateGame_updatesPlayer() throws DataAccessException {
        GameData game = new GameData(1, "testGame", null, null, new ChessGame());
        gameDAO.insertGame(game);

        gameDAO.updateGame(1, "WHITE", "alice");

        GameData updated = gameDAO.getGame(1);
        assertEquals("alice", updated.whiteUsername());
    }
}
