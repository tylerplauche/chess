package dataaccess.sql;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

public class MySqlDataAccess implements DataAccess {
    private final UserDAO userDao = new UserDAOSQL();
    private final GameDAO gameDao = new GameDAOSQL();
    private final AuthTokenDAO authDao = new AuthTokenDAOSQL();

    @Override
    public void clear() throws DataAccessException {
        authDao.clear();
        gameDao.clear();
        userDao.clear();
    }

    @Override
    public void insertUser(UserData user) throws DataAccessException {
        userDao.insertUser(user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return userDao.getUser(username);
    }

    @Override
    public void insertAuth(AuthData auth) throws DataAccessException {
        authDao.insertToken(auth);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return authDao.getToken(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        authDao.deleteToken(authToken);
    }

    @Override
    public int insertGame(GameData game) throws DataAccessException {
        return gameDao.insertGame(game);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return gameDao.getGame(gameID);
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return gameDao.listGames();
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {

        if (game.whiteUsername() != null) {
            gameDao.updateGame(game.gameID(), "WHITE", game.whiteUsername());
        }
        if (game.blackUsername() != null) {
            gameDao.updateGame(game.gameID(), "BLACK", game.blackUsername());
        }
    }
}
