package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import dataaccess.MemoryDataAccess;
import model.RegisterRequest;
import model.RegisterResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterServiceTest {
    private DataAccess data;
    private RegisterService service;

    @BeforeEach
    public void setup() {
        data = new MemoryDataAccess();
        service = new RegisterService(data);
    }

    @Test
    public void positiveRegisterTest() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("alice", "pass123", "alice@mail.com");
        RegisterResult result = service.register(request);
        assertEquals("alice", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void negativeRegisterTestAlreadyTaken() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("bob", "pass", "bob@mail.com");
        service.register(request);
        assertThrows(DataAccessException.class, () -> service.register(request));
    }
}
