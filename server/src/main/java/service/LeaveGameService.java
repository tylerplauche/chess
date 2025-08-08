package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

public class LeaveGameService {
    private final DataAccess db;

    public LeaveGameService(DataAccess db) {
        this.db = db;
    }

    public void leaveGame(String authToken, int gameID) throws DataAccessException {
        AuthData auth = db.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        GameData oldGame = db.getGame(gameID);
        if (oldGame == null) {
            throw new DataAccessException("Error: game not found");
        }

        String username = auth.username();

        String newWhite = oldGame.whiteUsername();
        String newBlack = oldGame.blackUsername();

        if (username.equals(newWhite)) {
            newWhite = null;
        } else if (username.equals(newBlack)) {
            newBlack = null;
        } else {
            throw new DataAccessException("Error: user not part of game");
        }

        GameData updatedGame = new GameData(
                oldGame.gameID(),
                newWhite,
                newBlack,
                oldGame.gameName(),
                oldGame.game()
        );

        db.updateGame(updatedGame);
    }
}
