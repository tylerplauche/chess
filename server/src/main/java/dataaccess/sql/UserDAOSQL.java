package dataaccess.sql;

import dataaccess.*;
import model.UserData;

import java.sql.*;

public class UserDAOSQL implements UserDAO {

    public void insertUser(UserData user) throws DataAccessException {
        String sql = "INSERT INTO user (username, password_hash, email) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.username());
            stmt.setString(2, user.passwordHash());
            stmt.setString(3, user.email());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Insert failed", ex);
        }
    }

    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT * FROM user WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new UserData(rs.getString("username"), rs.getString("password_hash"), rs.getString("email"));
            }
            return null;
        } catch (SQLException ex) {
            throw new DataAccessException("Get failed", ex);
        }
    }

    public void clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM user");
        } catch (SQLException ex) {
            throw new DataAccessException("Clear failed", ex);
        }
    }
}
