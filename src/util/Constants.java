package src.util;

public final class Constants {

    private Constants() {
    }

    // API URLs and keys - Use environment variables or config files ideally
    public static final String CEYFISH_API_BASE_URL = "https://api.ceyfish.lk/v1/";
    public static final String GOVERNMENT_FISHERIES_API_BASE_URL = "https://fisheries.gov.lk/api/";

    public static final String OPENWEATHERMAP_API_BASE_URL = "https://api.openweathermap.org/data/2.5/";
    public static final String OPENWEATHERMAP_API_KEY = System.getenv("OPENWEATHERMAP_API_KEY") != null ?
            System.getenv("OPENWEATHERMAP_API_KEY") : "YOUR_OPENWEATHERMAP_API_KEY";

    public static final String CEYFISH_API_KEY = System.getenv("CEYFISH_API_KEY") != null ?
            System.getenv("CEYFISH_API_KEY") : "YOUR_CEYFISH_API_KEY";

    public static final String FISHERIES_GOV_API_KEY = System.getenv("FISHERIES_GOV_API_KEY") != null ?
            System.getenv("FISHERIES_GOV_API_KEY") : "YOUR_GOV_FISHERIES_API_KEY";

    // FAO fisheries data location or endpoint
    public static final String FAO_FISHERIES_DATA_URL = "https://www.fao.org/fishery/api/geodata";

    // WebSocket chat server URL
    public static final String CHAT_SERVER_WS_URI = "ws://localhost:8080/chat";

    // Database settings - Ideally from env or config
    public static final String DB_URL = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : "jdbc:mysql://localhost:3306/smart_fisheries?useSSL=false&serverTimezone=UTC";
    public static final String DB_USER = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "root";
    public static final String DB_PASSWORD = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "";

    // Timeouts and Intervals (ms)
    public static final int API_TIMEOUT_MS = 10_000;
    public static final int MARKET_PRICE_REFRESH_INTERVAL_MS = 300_000; // 5 minutes
    public static final int WEATHER_REFRESH_INTERVAL_MS = 300_000; // 5 minutes
    public static final int FISH_DISTRIBUTION_REFRESH_INTERVAL_MS = 3600_000; // 1 hour
}
