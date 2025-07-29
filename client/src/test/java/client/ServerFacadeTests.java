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
    private static int port;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        System.out.println("Started test HTTP server on " + port);

    }
    @BeforeEach
    void setUp() throws Exception{
        facade = new ServerFacade("http://localhost:" + port);
        facade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    void registerSuccess() throws Exception {
        var auth = facade.register("player1", "pass", "p1@email.com");
        assertNotNull(auth);
        assertTrue(auth.authToken().length() > 10);
        assertEquals("player1", auth.username());
    }

    @Test
    void registerDuplicateFails() throws Exception {
        facade.register("player1", "pass", "p1@email.com");
        assertThrows(Exception.class, () ->
                facade.register("player1", "pass", "p1@email.com")
        );
    }



    @Test
    void loginSuccess() throws Exception {
        facade.register("user", "secret", "u@email.com");
        var auth = facade.login("user", "secret");
        assertEquals("user", auth.username());
    }

    @Test
    void loginWrongPasswordFails() throws Exception {
        facade.register("user", "correct", "u@email.com");
        assertThrows(Exception.class, () -> facade.login("user", "wrong"));
    }

    @Test
    void loginUnknownUserFails() {
        assertThrows(Exception.class, () -> facade.login("ghost", "pass"));
    }

    @Test
    void logoutSuccess() throws Exception {
        var auth = facade.register("logoutUser", "pass", "l@email.com");
        facade.logout(auth.authToken());
        assertThrows(Exception.class, () -> facade.logout(auth.authToken())); // token now invalid
    }

    @Test
    void logoutInvalidTokeFails() {
        assertThrows(Exception.class, () -> facade.logout("bad-token"));
    }

    @Test
    void createGameSuccess() throws Exception {
        var auth = facade.register("gamer", "pw", "g@email.com");
        var game = facade.createGame(auth.authToken(), "TestGame");
        assertEquals("TestGame", game.gameName());
        assertTrue(game.gameID() > 0);
    }

    @Test
    void createGameInvalidTokenFails() {
        assertThrows(Exception.class, () -> facade.createGame("invalid", "Game"));
    }

    @Test
    public void listGamesReturnsCreatedGame() throws Exception {
        var auth = facade.register("erin", "secret", "erin@example.com");
        var created = facade.createGame(auth.authToken(), "Erin's Game");

        var games = facade.listGames(auth.authToken());
        assertTrue(games.stream().anyMatch(g -> g.gameName().equals("Erin's Game")));
    }

    @Test
    void listGamesSuccess() throws Exception {
        var auth = facade.register("listUser", "pw", "l@email.com");
        facade.createGame(auth.authToken(), "Game1");
        var games = facade.listGames(auth.authToken());
        assertTrue(games.stream().anyMatch(g -> g.gameName().equals("Game1")));
    }

    @Test
    void listGamesInvalidTokenFails() {
        assertThrows(Exception.class, () -> facade.listGames("bad-token"));
    }

    @Test
    void joinGameAsWhiteSuccess() throws Exception {
        var auth = facade.register("joiner", "pw", "j@email.com");
        var game = facade.createGame(auth.authToken(), "JoinGame");
        facade.joinGame(auth.authToken(), game.gameID(), "WHITE"); // no exception = pass
    }


    @Test
    void joinGameInvalidTokenFails() {
        assertThrows(Exception.class, () -> facade.joinGame("invalid-token", 1, "BLACK"));
    }
    @Test
    void clearDeletesUsers() throws Exception {
        facade.register("temp", "pw", "t@email.com");
        facade.clear();

        assertThrows(Exception.class, () -> facade.login("temp", "pw"));
    }



}
