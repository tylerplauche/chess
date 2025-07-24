package dataaccess;

import chess.ChessGame;
import dataaccess.sql.GameDAOSQL;
import dataaccess.sql.UserDAOSQL;  // Import your UserDAO implementation
import model.GameData;
import model.UserData;                // Assuming you have a User model class
import org.junit.jupiter.api.*;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class GameDAOTest {

    private GameDAO gameDAO;
    private UserDAO userDAO;  // Add userDAO for managing users

    @BeforeEach
    public void setUp() throws DataAccessException {
        gameDAO = new GameDAOSQL();
        userDAO = new UserDAOSQL();    // Initialize userDAO
        gameDAO.clear();
        userDAO.clear();                // Also clear user table to start fresh
    }


    @Test
    public void insertGameSuccess() throws DataAccessException {
        GameData game = new GameData(0, null, null, "testGame", new ChessGame());
        int generatedId = gameDAO.insertGame(game);

        GameData retrieved = gameDAO.getGame(generatedId);
        assertNotNull(retrieved);
        assertEquals("testGame", retrieved.gameName());
    }

    @Test
    public void insertGameDuplicateNameFails() throws DataAccessException {
        GameData game1 = new GameData(0, null, null, "uniqueGame", new ChessGame());
        gameDAO.insertGame(game1);

        GameData game2 = new GameData(0, null, null, "uniqueGame", new ChessGame());
        assertThrows(DataAccessException.class, () -> gameDAO.insertGame(game2));
    }

    @Test
    public void getGameNotFoundReturnsNull() throws DataAccessException {
        assertNull(gameDAO.getGame(9999)); // unlikely ID
    }

    @Test
    public void getAllGamesReturnsGames() throws DataAccessException {
        gameDAO.insertGame(new GameData(0, null, null, "gameOne", new ChessGame()));
        gameDAO.insertGame(new GameData(0, null, null, "gameTwo", new ChessGame()));

        Collection<GameData> games = gameDAO.listGames();
        assertEquals(2, games.size());
    }

    @Test
    public void updateGameUpdatesPlayer() throws DataAccessException {

        userDAO.insertUser(new UserData("alice", "passwordHash", "alice@example.com"));

        GameData game = new GameData(0, null, null, "testGame", new ChessGame());
        int gameId = gameDAO.insertGame(game);

        gameDAO.updateGame(gameId, "WHITE", "alice");

        GameData updated = gameDAO.getGame(gameId);
        assertNotNull(updated);
        assertEquals("alice", updated.whiteUsername());
    }
}
