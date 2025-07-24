package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.LoginRequest;
import model.LoginResult;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

public class LoginServiceTest {
    private DataAccess db;
    private LoginService loginService;
    private final String username = "user";
    private final String password = "pass";
    private final String email = "email@test.com";

    @BeforeEach
    public void setUp() throws DataAccessException {
        db = new MemoryDataAccess();
        db.insertUser(new UserData(username, password, email));
        loginService = new LoginService(db);
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        UserData user = new UserData(username, hashedPassword, "test@example.com");
        db.insertUser(user);

    }

    @Test
    public void loginSuccess() throws DataAccessException {
        // Create login request with raw password
        LoginRequest request = new LoginRequest(username, password);

        // Call login service
        LoginResult result = loginService.login(request);

        // Verify login result
        assertNotNull(result);
        assertEquals(username, result.username());
        assertNotNull(result.authToken());

        // Verify auth token stored in DB
        AuthData auth = db.getAuth(result.authToken());
        assertNotNull(auth);
        assertEquals(username, auth.username());
    }


    @Test
    public void loginWrongPassword() {
        LoginRequest request = new LoginRequest(username, "wrong");
        DataAccessException ex = assertThrows(DataAccessException.class,
                () -> loginService.login(request));
        assertEquals("unauthorized", ex.getMessage());
    }

    @Test
    public void loginUserDoesNotExist() {
        LoginRequest request = new LoginRequest("nonexistent", "pass");
        DataAccessException ex = assertThrows(DataAccessException.class,
                () -> loginService.login(request));
        assertEquals("unauthorized", ex.getMessage());
    }

    @Test
    public void loginMissingFields() {
        LoginRequest request = new LoginRequest(null, password);
        DataAccessException ex = assertThrows(DataAccessException.class,
                () -> loginService.login(request));
        assertEquals("bad request", ex.getMessage());
    }
}
