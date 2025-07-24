package dataaccess.sql;

import com.google.gson.Gson;
import dataaccess.*;
import model.GameData;
import chess.ChessGame;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class GameDAOSQL implements GameDAO {

    // GameDAOSQL insertGame
    public int insertGame(GameData game) throws DataAccessException {
        String sql = "INSERT INTO game (white_username, black_username, game_name, game_state) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, game.gameName());
            stmt.setString(4, SerializationUtils.serializeGame(game.game()));
            int rowsInserted = stmt.executeUpdate();
            System.out.println("insertGame: Rows inserted = " + rowsInserted + " for game " + game.gameName());

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                System.out.println("insertGame: Generated ID = " + generatedId);
                return generatedId;
            } else {
                throw new DataAccessException("Game ID not returned after insert.");
            }
        } catch (SQLException ex) {
            System.out.println("insertGame: SQLException " + ex.getMessage());
            throw new DataAccessException("Insert failed: " + ex.getMessage(), ex);
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
                return new GameData(
                        rs.getInt("id"),
                        rs.getString("white_username"),
                        rs.getString("black_username"),
                        rs.getString("game_name"),
                        game
                );
            }

            return null;
        } catch (SQLException ex) {
            throw new DataAccessException("Get game failed: " + ex.getMessage(), ex);
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
                games.add(new GameData(
                        rs.getInt("id"),
                        rs.getString("white_username"),
                        rs.getString("black_username"),
                        rs.getString("game_name"),
                        game
                ));
            }

            return games;
        } catch (SQLException ex) {
            throw new DataAccessException("List games failed: " + ex.getMessage(), ex);
        }
    }

    // New helper method to check if user exists in 'user' table
    private boolean userExists(String username) throws DataAccessException {
        String sql = "SELECT 1 FROM user WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException ex) {
            throw new DataAccessException("User existence check failed: " + ex.getMessage(), ex);
        }
    }

    public void updateGame(int gameId, String playerColor, String username) throws DataAccessException {
        if (!userExists(username)) {
            throw new DataAccessException("Username does not exist: " + username);
        }

        String sql;
        if (playerColor.equalsIgnoreCase("WHITE")) {
            sql = "UPDATE game SET white_username = ? WHERE id = ?";
        } else if (playerColor.equalsIgnoreCase("BLACK")) {
            sql = "UPDATE game SET black_username = ? WHERE id = ?";
        } else {
            throw new DataAccessException("Invalid player color: " + playerColor);
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setInt(2, gameId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Update game failed: " + ex.getMessage(), ex);
        }
    }

    public void clear() throws DataAccessException {
        String sql = "DELETE FROM game";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);
        } catch (SQLException ex) {
            throw new DataAccessException("Clear failed: " + ex.getMessage(), ex);
        }
    }
}
