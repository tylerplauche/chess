// service/JoinGameService.java
package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.JoinGameRequest;
import model.JoinGameResult;

public class JoinGameService {
    private final DataAccess data;

    public JoinGameService(DataAccess data) {
        this.data = data;
    }

    public JoinGameResult joinGame(String authToken, JoinGameRequest request) throws DataAccessException {
        if (authToken == null || request.playerColor() == null || request.gameID() == null) {
            throw new DataAccessException("bad request");
        }

        AuthData auth = data.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }

        GameData game = data.getGame(request.gameID());
        if (game == null) {
            throw new DataAccessException("bad request");
        }

        String username = auth.username();
        GameData updatedGame;

        if ("white".equalsIgnoreCase(request.playerColor())) {
            if (game.whiteUsername() != null) {
                throw new DataAccessException("already taken");
            }
            updatedGame = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
        } else if ("black".equalsIgnoreCase(request.playerColor())) {
            if (game.blackUsername() != null) {
                throw new DataAccessException("already taken");
            }
            updatedGame = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
        } else {
            throw new DataAccessException("bad request");
        }

        data.updateGame(updatedGame);
        return new JoinGameResult();
    }
}
