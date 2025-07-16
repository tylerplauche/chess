package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.CreateGameRequest;
import model.CreateGameResult;
import model.GameData;

public class GameService {
    private final DataAccess db;

    public GameService(DataAccess db) {
        this.db = db;
    }

    public CreateGameResult createGame(String authToken, CreateGameRequest request) throws DataAccessException {
        if (request.gameName() == null || request.gameName().isBlank()) {
            throw new DataAccessException("bad request");
        }

        AuthData auth = db.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }

        GameData game = new GameData(0, null, null, request.gameName(), null);
        int gameID = db.insertGame(game);
        return new CreateGameResult(gameID);
    }
}
