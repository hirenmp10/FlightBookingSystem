package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.User;
import db.DBConnection;

public class UserDAO {

    // Add a new user
    public static boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password, role, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getEmail());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("SQL Error during registration: " + e.getMessage());
            return false;
        }
    }

    public static User getUserById(int userId) {
        String query = "SELECT id, username, password, role, email FROM users WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error in getUserById: " + e.getMessage());
        }
        return null;
    }

    public static boolean updateUser(int userId, String username, String password) {
        // First check if username is taken (by another user)
        String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ? AND id != ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            
            checkStmt.setString(1, username);
            checkStmt.setInt(2, userId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return false;
            }

            String updateQuery = "UPDATE users SET username = ?, password = ? WHERE id = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                updateStmt.setString(1, username);
                updateStmt.setString(2, password);
                updateStmt.setInt(3, userId);
                return updateStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Database error in updateUser: " + e.getMessage());
            return false;
        }
    }

    private static User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = utils.UserFactory.createUser(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("role")
        );
        user.setEmail(rs.getString("email"));
        return user;
    }

    // Fetch a user based on username and password
    public static User getUser(String username, String password) {
        String sql = "SELECT id, username, password, role, email FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error in getUser: " + e.getMessage());
        }
        return null;
    }

    // Fetch all users
    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, password, role, email FROM users";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Database error in getAllUsers: " + e.getMessage());
        }
        return users;
    }

    // Delete a user by ID
    public static boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error during user deletion: " + e.getMessage());
            return false;
        }
    }

    // Update a user's role
    public static boolean updateUserRole(int userId, String newRole) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newRole);
            stmt.setInt(2, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error during user role update: " + e.getMessage());
            return false;
        }
    }
}
