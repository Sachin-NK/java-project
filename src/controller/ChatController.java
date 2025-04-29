package src.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import src.model.ChatMessage;
import src.service.ChatService;
import src.service.ChatbotService;

import java.time.format.DateTimeFormatter;

public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @FXML
    private ListView<String> chatListView;
    @FXML
    private TextArea messageInputArea;
    @FXML
    private Button sendButton;
    @FXML
    private CheckBox chatbotToggle;

    private final ObservableList<String> chatMessages = FXCollections.observableArrayList();

    private ChatService chatService;
    private ChatbotService chatbotService;

    private String currentUsername = "anonymous";

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public ChatController() {
    }

    @FXML
    public void initialize() {
        chatListView.setItems(chatMessages);
        sendButton.setDisable(true);

        messageInputArea.textProperty().addListener((obs, oldVal, newVal) -> {
            sendButton.setDisable(newVal == null || newVal.trim().isEmpty());
        });

        sendButton.setOnAction(e -> sendMessage());

        chatbotToggle.setSelected(true);
    }

    public void setServices(ChatService chatService, ChatbotService chatbotService) {
        this.chatService = chatService;
        this.chatbotService = chatbotService;

        this.chatService.setOnMessageReceived(this::onNewMessage);
        this.chatService.setOnConnectionStatusChanged(this::onConnectionStatusChanged);

        // For testing convenience, user is anonymous; can be enhanced for login
        currentUsername = "user" + System.currentTimeMillis() % 10000;
    }

    private void onNewMessage(ChatMessage message) {
        Platform.runLater(() -> {
            String formatted = formatChatMessage(message);
            chatMessages.add(formatted);
            chatListView.scrollTo(chatMessages.size() - 1);
        });
    }

    private void onConnectionStatusChanged(Boolean isConnected) {
        Platform.runLater(() -> {
            String systemMsg = isConnected ? "Connected to chat server." : "Disconnected from chat server.";
            chatMessages.add("[System]: " + systemMsg);
            chatListView.scrollTo(chatMessages.size() - 1);
            sendButton.setDisable(!isConnected);
        });
    }

    private void sendMessage() {
        String msg = messageInputArea.getText().trim();
        if (msg.isEmpty() || chatService == null) return;

        chatService.sendMessage(msg, currentUsername);

        if (chatbotToggle.isSelected()) {
            ChatMessage botResponse = chatbotService.respondTo(msg, currentUsername);
            if (botResponse != null) {
                onNewMessage(botResponse);
            }
        }

        messageInputArea.clear();
    }

    private String formatChatMessage(ChatMessage message) {
        String time = timeFormatter.format(message.getTimestamp());
        if ("system".equalsIgnoreCase(message.getMessageType())) {
            return String.format("[%s] %s: %s", time, message.getSenderUsername(), message.getMessage());
        }
        return String.format("[%s] %s: %s", time, message.getSenderUsername(), message.getMessage());
    }
}
