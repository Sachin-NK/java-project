package src.db.dao;

import org.mindrot.jbcrypt.BCrypt;
import src.db.DatabaseManager;
import src.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final DatabaseManager dbManager;

    public UserDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public boolean createUser(String username, String plainPassword, String displayName, String contactInfo, String permissions) throws SQLException {
        if (getUserByUsername(username) != null) return false;
        String sql = "INSERT INTO users (username, password_hash, display_name, contact_info, permissions) VALUES (?, ?, ?, ?, ?)";
        String hashed = hashPassword(plainPassword);
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hashed);
            stmt.setString(3, displayName);
            stmt.setString(4, contactInfo);
            stmt.setString(5, permissions);
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }

    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT username, password_hash, display_name, contact_info, permissions FROM users WHERE username = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("display_name"),
                            rs.getString("contact_info"),
                            rs.getString("permissions")
                    );
                }
            }
        }
        return null;
    }

    public boolean authenticateUser(String username, String plainPassword) throws SQLException {
        User user = getUserByUsername(username);
        if (user == null) return false;
        return checkPassword(plainPassword, user.getPasswordHash());
    }

    public boolean updateUserDisplayName(String username, String newDisplayName) throws SQLException {
        String sql = "UPDATE users SET display_name = ? WHERE username = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newDisplayName);
            stmt.setString(2, username);
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT username, password_hash, display_name, contact_info, permissions FROM users";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(new User(
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("display_name"),
                        rs.getString("contact_info"),
                        rs.getString("permissions")
                ));
            }
        }
        return users;
    }

    private String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    private boolean checkPassword(String plainPassword, String hashed) {
        return BCrypt.checkpw(plainPassword, hashed);
    }
}
