package server;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import spark.Spark;
import service.RegisterService;
import service.LoginService;
import service.ClearService;
import server.LoginHandler;
import server.ClearHandler;
import server.RegisterHandler;

public class Server {
    private  DataAccess db = new MemoryDataAccess();


    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", new ClearHandler(db));
        Spark.post("/user", new RegisterHandler(db));
        Spark.post("/session", new LoginHandler(db));
        Spark.post("/game", new CreateGameHandler(db));


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
