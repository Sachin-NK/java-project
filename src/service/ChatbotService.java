package src.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import src.model.ChatMessage;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ChatbotService {
    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    private final Map<String, String> faqResponses;

    public ChatbotService() {
        faqResponses = new HashMap<>();
        initFaqs();
    }

    public void init() {
        logger.info("ChatbotService initialized.");
    }

    private void initFaqs() {
        faqResponses.put("hello", "Hello! Welcome to Smart Fisheries Management System. How can I assist you today?");
        faqResponses.put("help", "You can ask me about fish market prices, weather alerts, fish species classification, or chat features.");
        faqResponses.put("fish prices", "Real-time fish market prices are available on the Dashboard tab. You can refresh data anytime.");
        faqResponses.put("weather", "Weather updates and safety alerts are provided on the Dashboard for major locations in Sri Lanka.");
        faqResponses.put("classify fish", "In Species Classifier tab, upload a fish image to identify the species with detailed info.");
        faqResponses.put("chat", "The Chat tab lets you communicate with other fishermen in real-time.");
        faqResponses.put("navigation", "Use the top tab menu to switch between Dashboard, Chat, Species Classifier, and Settings.");
        faqResponses.put("thanks", "You're welcome! Feel free to ask if you need anything else.");
        faqResponses.put("thank you", "You're welcome! Always here to help.");
    }

    public ChatMessage respondTo(String userInput, String username) {
        try {
            String normalized = userInput.toLowerCase().trim();
            String responseText = "Sorry, I couldn't understand your question. Please try rephrasing or ask for help.";

            for (String key : faqResponses.keySet()) {
                if (normalized.contains(key)) {
                    responseText = faqResponses.get(key);
                    break;
                }
            }
            return new ChatMessage("Chatbot", responseText, LocalDateTime.now(), "system");
        } catch (Exception e) {
            logger.error("Error in chatbot response generation", e);
            return new ChatMessage("Chatbot", "An error occurred while processing your request.", LocalDateTime.now(), "system");
        }
    }
}
