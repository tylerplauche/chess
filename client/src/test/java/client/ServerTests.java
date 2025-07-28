package client;

import server.Server;
import ui.ServerFacade;
import model.AuthData;
import model.UserData;
import model.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class ServerTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    public static void stopServer() {
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
    public void createGameNoAuthFails() {
        assertThrows(Exception.class, () -> facade.createGame("bad-token", "Game 1"));
    }

    @Test
    public void listGamesInvalidTokenFails() {
        assertThrows(Exception.class, () -> facade.listGames("invalid-token"));
    }
}
