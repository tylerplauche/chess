// service/ListGameService.java
package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.ListGamesResult;

import java.util.Collection;

public class ListGameService {
    private final DataAccess data;

    public ListGameService(DataAccess data) {
        this.data = data;
    }

    public ListGamesResult listGames(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new DataAccessException("unauthorized");
        }

        AuthData auth = data.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }

        Collection<GameData> games = data.listGames();
        return new ListGamesResult(games);
    }
}
