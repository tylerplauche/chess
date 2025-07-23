package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.CreateGameRequest;
import model.CreateGameResult;
import model.GameData;
import chess.ChessGame;

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

        // Set white player to authenticated user, black player null for now
        String whiteUsername = null;

        // Create new ChessGame instance or null if you want to defer creation
        ChessGame initialGameState = new ChessGame();

        GameData game = new GameData(
                0,              // id will be assigned by DB
                whiteUsername,  // white player
                null,           // black player not assigned yet
                request.gameName(),
                initialGameState
        );

        int gameID = db.insertGame(game);
        return new CreateGameResult(gameID);
    }
}
