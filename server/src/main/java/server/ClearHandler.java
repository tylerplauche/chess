package server;


import com.google.gson.Gson;
import dataaccess.DataAccess;
import service.ClearService;
import spark.Request;
import spark.Response;
import spark.Route;

public class ClearHandler implements Route {
    private final ClearService service;

    public ClearHandler(DataAccess db) {
        this.service = new ClearService(db);
    }

    public Object handle(Request req, Response res) {
        service.clear();
        res.status(200);
        return new Gson().toJson(new Object()); // returns {}
    }
}


