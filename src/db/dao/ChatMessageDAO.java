package src.db.dao;

import src.db.DatabaseManager;
import src.model.ChatMessage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatMessageDAO {

    private final DatabaseManager dbManager;

    public ChatMessageDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public void insertMessage(ChatMessage message) throws SQLException {
        String sql = "INSERT INTO chat_messages (sender_username, message, timestamp, message_type) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, message.getSenderUsername());
            stmt.setString(2, message.getMessage());
            stmt.setTimestamp(3, Timestamp.valueOf(message.getTimestamp()));
            stmt.setString(4, message.getMessageType());
            stmt.executeUpdate();
        }
    }

    public List<ChatMessage> getMessagesByUser(String username, int limit) throws SQLException {
        List<ChatMessage> messages = new ArrayList<>();
        String sql = "SELECT sender_username, message, timestamp, message_type FROM chat_messages WHERE sender_username = ? ORDER BY timestamp DESC LIMIT ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ChatMessage msg = new ChatMessage(
                            rs.getString("sender_username"),
                            rs.getString("message"),
                            rs.getTimestamp("timestamp").toLocalDateTime(),
                            rs.getString("message_type")
                    );
                    messages.add(msg);
                }
            }
        }
        return messages;
    }

    public List<ChatMessage> getRecentMessages(int limit) throws SQLException {
        List<ChatMessage> messages = new ArrayList<>();
        String sql = "SELECT sender_username, message, timestamp, message_type FROM chat_messages ORDER BY timestamp DESC LIMIT ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ChatMessage msg = new ChatMessage(
                            rs.getString("sender_username"),
                            rs.getString("message"),
                            rs.getTimestamp("timestamp").toLocalDateTime(),
                            rs.getString("message_type")
                    );
                    messages.add(msg);
                }
            }
        }
        return messages;
    }

    public void deleteOldMessages(int olderThanDays) throws SQLException {
        String sql = "DELETE FROM chat_messages WHERE timestamp < NOW() - INTERVAL ? DAY";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, olderThanDays);
            stmt.executeUpdate();
        }
    }
}
