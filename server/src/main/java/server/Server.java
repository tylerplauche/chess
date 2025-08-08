package server;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.MemoryDataAccess;
import dataaccess.sql.MySqlDataAccess;
import spark.Spark;
import service.LogoutService;

import static spark.Spark.webSocket;
import websocket.ChessWebSocketHandler;

public class Server {
    private  DataAccess db = new MySqlDataAccess();
    LogoutService logoutService = new LogoutService(db);


    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        webSocket("/ws", ChessWebSocketHandler.class);



        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", new ClearHandler(db));
        Spark.post("/user", new RegisterHandler(db));
        Spark.post("/session", new LoginHandler(db));
        Spark.post("/game", new CreateGameHandler(db));
        Spark.get("/game", new ListGamesHandler(db));
        Spark.put("/game", new JoinGameHandler(db));
        Spark.put("/game/:gameID/leave", new LeaveGameHandler(db));
        Spark.delete("/session", new LogoutHandler(logoutService));


        //This line initializes the server and can be removed once you have a functioning endpoint
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}

