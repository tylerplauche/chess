package dataaccess;

import model.GameData;
import java.util.Collection;

public interface GameDAO {

    int insertGame(GameData game) throws DataAccessException;

    GameData getGame(int gameID) throws DataAccessException;

    Collection<GameData> listGames() throws DataAccessException;

    void updateGame(GameData game) throws DataAccessException;

    void clear() throws DataAccessException;

    Collection<GameData> getAllGames() throws DataAccessException;

}
