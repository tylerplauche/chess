package dataaccess.sql;

import com.google.gson.Gson;
import dataaccess.*;
import model.GameData;
import chess.ChessGame;
import java.util.ArrayList;
import java.util.Collection;

import java.sql.*;

public class GameDAOSQL implements GameDAO {

    public int insertGame(GameData game) throws DataAccessException {
        String sql = "INSERT INTO game (white_username, black_username, game_name, game_state) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, game.gameName());
            stmt.setString(4, SerializationUtils.serializeGame(game.game()));
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new DataAccessException("Game ID not returned");
        } catch (SQLException ex) {
            throw new DataAccessException("Insert failed", ex);
        }
    }

    public GameData getGame(int gameId) throws DataAccessException {
        String sql = "SELECT * FROM game WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                ChessGame game = SerializationUtils.deserializeGame(rs.getString("game_state"));
                return new GameData(rs.getInt("id"), rs.getString("white_username"),
                        rs.getString("black_username"), rs.getString("game_name"), game);
            }
            return null;
        } catch (SQLException ex) {
            throw new DataAccessException("Get failed", ex);
        }
    }

    public Collection<GameData> listGames() throws DataAccessException {
        String sql = "SELECT * FROM game";
        Collection<GameData> games = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ChessGame game = SerializationUtils.deserializeGame(rs.getString("game_state"));
                GameData gameData = new GameData(
                        rs.getInt("id"),
                        rs.getString("white_username"),
                        rs.getString("black_username"),
                        rs.getString("game_name"),
                        game
                );
                games.add(gameData);
            }

            return games;
        } catch (SQLException ex) {
            throw new DataAccessException("List games failed", ex);
        }
    }

    @Override
    public void updateGame(int gameID, String playerColor, String username) throws DataAccessException {
        String sql;
        if (playerColor.equalsIgnoreCase("WHITE")) {
            sql = "UPDATE game SET whiteUsername = ? WHERE id = ?";
        } else if (playerColor.equalsIgnoreCase("BLACK")) {
            sql = "UPDATE game SET blackUsername = ? WHERE id = ?";
        } else {
            throw new DataAccessException("Invalid player color: " + playerColor);
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setInt(2, gameID);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("Unable to update game", e);
        }
    }


    @Override
    public Collection<GameData> getAllGames() throws DataAccessException {
        ArrayList<GameData> games = new ArrayList<>();
        String sql = "SELECT * FROM game";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String white = rs.getString("whiteUsername");
                String black = rs.getString("blackUsername");
                String gameJson = rs.getString("game");

                ChessGame game = new Gson().fromJson(gameJson, ChessGame.class);
                games.add(new GameData(id, name, white, black, game));
            }

            return games;
        } catch (Exception e) {
            throw new DataAccessException("Unable to get games", e);
        }
    }



    public void clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM game");
        } catch (SQLException ex) {
            throw new DataAccessException("Clear failed", ex);
        }
    }
}
