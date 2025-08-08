package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.*;

public class MemoryDataAccess implements DataAccess { //Class 'MemoryDataAccess' must either be declared abstract or implement abstract method 'updateGameState(int, String)' in 'DataAcces
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> auths = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;



    public void clear() {
        users.clear();
        auths.clear();
        games.clear();
        nextGameID = 1;
    }


    public void insertUser(UserData user) throws DataAccessException {

        UserData hashedUser = new UserData(user.username(), user.password(), user.email());
        users.put(user.username(), hashedUser);
    }

    public UserData getUser(String username) throws DataAccessException {
        return users.get(username);
    }

    @Override
    public void updateGameState(int gameID, String gameStateJson) throws DataAccessException {

    }

    public void insertAuth(AuthData auth) throws DataAccessException {
        auths.put(auth.authToken(), auth);
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        return auths.get(authToken);
    }

    public void deleteAuth(String authToken) throws DataAccessException {
        auths.remove(authToken);
    }

    public int insertGame(GameData game) throws DataAccessException {
        int id = nextGameID;
        nextGameID++;
        GameData newGame = new GameData(id, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
        games.put(id, newGame);
        return id;
    }

    public GameData getGame(int gameID) throws DataAccessException {
        return games.get(gameID);
    }

    public Collection<GameData> listGames() throws DataAccessException {
        return games.values();
    }

    public void updateGame(GameData game) throws DataAccessException {
        games.put(game.gameID(), game);
    }
}

