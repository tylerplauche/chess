package dataaccess.sql;

import dataaccess.*;
import model.AuthData;

import java.sql.*;

public class AuthTokenDAOSQL implements AuthTokenDAO {

    public void insertToken(AuthData token) throws DataAccessException {
        String sql = "INSERT INTO auth_token (token, username) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token.authToken());
            stmt.setString(2, token.username());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Insert failed", ex);
        }
    }

    public AuthData getToken(String tokenValue) throws DataAccessException {
        String sql = "SELECT * FROM auth_token WHERE token = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tokenValue);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new AuthData(rs.getString("token"), rs.getString("username"));
            }
            return null;
        } catch (SQLException ex) {
            throw new DataAccessException("Get failed", ex);
        }
    }

    public void deleteToken(String token) throws DataAccessException {
        String sql = "DELETE FROM auth_token WHERE token = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Delete failed", ex);
        }
    }


    public void clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM auth_token");
        } catch (SQLException ex) {
            throw new DataAccessException("Clear failed", ex);
        }
    }
}
