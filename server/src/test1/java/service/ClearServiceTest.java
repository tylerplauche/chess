package service;

import dataaccess.MemoryDataAccess;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClearServiceTest {
    private MemoryDataAccess db;
    private ClearService service;

    @BeforeEach
    void setUp() {
        db = new MemoryDataAccess();
        service = new ClearService(db);
    }

    @Test
    void testClearPositive() {
        assertDoesNotThrow(() -> db.insertUser(new UserData("a", "b", "c")));
        service.clear();
        assertNull(db.getUser("a"));
    }
}
