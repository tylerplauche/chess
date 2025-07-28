package client;


import server.Server;
import ui.ServerFacade;
import model.AuthData;
import model.UserData;
import model.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }
    @BeforeEach
    void setUp() {
        facade = new ServerFacade("http://localhost:8080"); // Replace with your actual base URL
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void registerSuccess() throws Exception {
        AuthData auth = facade.register("alice", "password", "alice@example.com");
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals("alice", auth.username());
    }

    @Test
    public void registerDuplicateFails() throws Exception {
        facade.register("alice", "password", "alice@example.com");
        assertThrows(Exception.class, () -> {
            facade.register("alice", "password", "alice@example.com");
        });
    }

    @Test
    public void loginSuccess() throws Exception {
        facade.register("bob", "secret", "bob@example.com");
        AuthData auth = facade.login("bob", "secret");
        assertNotNull(auth);
        assertEquals("bob", auth.username());
    }

    @Test
    public void loginInvalidPasswordFails() throws Exception {
        facade.register("bob", "secret", "bob@example.com");
        assertThrows(Exception.class, () -> {
            facade.login("bob", "wrongpass");
        });
    }

    @Test
    public void loginUnknownUserFails() throws Exception {
        assertThrows(Exception.class, () -> {
            facade.login("ghost", "pass");
        });
    }
    @Test
    public void logoutSuccess() throws Exception {
        var auth = facade.register("charlie", "hunter2", "charlie@example.com");
        facade.logout(auth.authToken());
        // Trying to logout again should fail
        assertThrows(Exception.class, () -> facade.logout(auth.authToken()));
    }

    @Test
    public void logoutInvalidTokenFails() {
        assertThrows(Exception.class, () -> facade.logout("invalid-token"));
    }
    @Test
    public void createGameSuccess() throws Exception {
        var auth = facade.register("diana", "hunter2", "diana@example.com");
        var game = facade.createGame(auth.authToken(), "Game 1");
        assertNotNull(game);
        assertNotNull(game.gameID());
        assertEquals("Game 1", game.gameName());
    }

    @Test
    public void createGameNoAuthFails() {
        assertThrows(Exception.class, () -> facade.createGame("bad-token", "Game 1"));
    }
    @Test
    public void listGamesReturnsCreatedGame() throws Exception {
        var auth = facade.register("erin", "secret", "erin@example.com");
        var created = facade.createGame(auth.authToken(), "Erin's Game");

        var games = facade.listGames(auth.authToken());
        assertTrue(games.stream().anyMatch(g -> g.gameName().equals("Erin's Game")));
    }

    @Test
    public void listGamesInvalidTokenFails() {
        assertThrows(Exception.class, () -> facade.listGames("invalid-token"));
    }
    @Test
    public void joinGameSuccess() throws Exception {
        var auth = facade.register("frank", "pass123", "frank@example.com");
        var game = facade.createGame(auth.authToken(), "Frank's Game");

        // Join as white
        facade.joinGame(auth.authToken(), game.gameID(), "WHITE");
        // Join as observer
        facade.joinGame(auth.authToken(), game.gameID(), null); // null for no color
    }

    @Test
    public void joinGameInvalidTokenFails() {
        assertThrows(Exception.class, () -> facade.joinGame("invalid", 1, "BLACK"));
    }

}
