package dataaccess;


import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

public interface DataAccess {
    void clear() throws DataAccessException;

    void insertUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    //GameData getGame(int gameID) throws DataAccessException;
    void updateGameState(int gameID, String gameStateJson) throws DataAccessException;

    void insertAuth(AuthData auth) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;

    int insertGame(GameData game) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    Collection<GameData> listGames() throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
}

