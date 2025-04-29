package src.service;

import org.glassfish.tyrus.client.ClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import src.db.dao.ChatMessageDAO;
import src.model.ChatMessage;
import src.util.Constants;

import javax.websocket.*;
import javax.websocket.OnError;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.function.Consumer;

@ClientEndpoint
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private Session userSession;
    private final ChatMessageDAO chatMessageDAO;

    private Consumer<ChatMessage> onMessageReceived;
    private Consumer<Boolean> onConnectionStatusChanged;

    private final Object connectionLock = new Object();

    private boolean running = false;

    public ChatService(src.db.DatabaseManager databaseManager) {
        this.chatMessageDAO = new ChatMessageDAO();
    }

    public void init() {
        running = true;
        connect();
    }

    public void shutdown() {
        running = false;
        if (userSession != null && userSession.isOpen()) {
            try {
                userSession.close();
            } catch (Exception e) {
                logger.warn("Error closing WebSocket session", e);
            }
        }
    }

    private void connect() {
        ClientManager client = ClientManager.createClient();
        try {
            client.connectToServer(this, new URI(Constants.CHAT_SERVER_WS_URI));
        } catch (Exception e) {
            logger.error("Failed to connect to chat server: {}", Constants.CHAT_SERVER_WS_URI, e);
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                if (running) {
                    logger.info("Reconnecting to chat server...");
                    connect();
                }
            } catch (InterruptedException ignored) {
            }
        }, "ChatService-ReconnectThread").start();
    }

    public void sendMessage(String message, String senderUsername) {
        if (userSession == null || !userSession.isOpen()) {
            logger.warn("WebSocket is not open. Cannot send message.");
            return;
        }
        try {
            ChatMessage chatMessage = new ChatMessage(senderUsername, message, LocalDateTime.now(), "user");
            String json = JsonUtil.toJsonString(chatMessage);
            userSession.getAsyncRemote().sendText(json);
            chatMessageDAO.insertMessage(chatMessage);
        } catch (Exception e) {
            logger.error("Failed to send chat message", e);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        logger.info("Connected to chat server.");
        this.userSession = session;
        if (onConnectionStatusChanged != null) {
            onConnectionStatusChanged.accept(true);
        }
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            ChatMessage chatMessage = JsonUtil.fromJsonString(message, ChatMessage.class);
            if (chatMessage != null) {
                if (onMessageReceived != null) {
                    onMessageReceived.accept(chatMessage);
                }
                chatMessageDAO.insertMessage(chatMessage);
            }
        } catch (Exception e) {
            logger.error("Error processing incoming chat message", e);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        logger.warn("Chat server connection closed: {}", closeReason);
        this.userSession = null;
        if (onConnectionStatusChanged != null) {
            onConnectionStatusChanged.accept(false);
        }
        if (running) {
            scheduleReconnect();
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("Chat server connection error", throwable);
    }

    public void setOnMessageReceived(Consumer<ChatMessage> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public void setOnConnectionStatusChanged(Consumer<Boolean> onConnectionStatusChanged) {
        this.onConnectionStatusChanged = onConnectionStatusChanged;
    }

    private static class JsonUtil {
        private static final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

        static String toJsonString(Object obj) {
            try {
                return mapper.writeValueAsString(obj);
            } catch (Exception e) {
                return "{}";
            }
        }

        static <T> T fromJsonString(String json, Class<T> clazz) {
            try {
                return mapper.readValue(json, clazz);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
