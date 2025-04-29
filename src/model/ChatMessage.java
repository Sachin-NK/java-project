package src.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class ChatMessage {
    private String senderUsername;
    private String message;
    private LocalDateTime timestamp;
    private String messageType; // user or system

    public ChatMessage() {
    }

    public ChatMessage(String senderUsername, String message, LocalDateTime timestamp, String messageType) {
        this.senderUsername = senderUsername;
        this.message = message;
        this.timestamp = timestamp;
        this.messageType = messageType;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMessage)) return false;
        ChatMessage that = (ChatMessage) o;
        return Objects.equals(senderUsername, that.senderUsername) &&
                Objects.equals(message, that.message) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(messageType, that.messageType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderUsername, message, timestamp, messageType);
    }
}
