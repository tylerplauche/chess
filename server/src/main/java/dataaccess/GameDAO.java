package dataaccess;

import model.GameData;
import java.util.Collection;

public interface GameDAO {

    int insertGame(GameData game) throws DataAccessException;

    GameData getGame(int gameID) throws DataAccessException;

    Collection<GameData> listGames() throws DataAccessException;

    void updateGame(int gameID, String playerColor, String username) throws DataAccessException;


    void clear() throws DataAccessException;


    void updateGameState(int gameId, String gameStateJson) throws DataAccessException;;
}
