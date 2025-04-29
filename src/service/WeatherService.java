package src.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import src.model.WeatherInfo;
import src.util.APIClient;
import src.util.Constants;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;


public class WeatherService {
    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    private final ScheduledExecutorService scheduler;
    private volatile Map<String, WeatherInfo> weatherDataByLocation;
    private final List<Consumer<Map<String, WeatherInfo>>> listeners;

    public WeatherService() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "WeatherService"));
        this.weatherDataByLocation = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
    }

    public void init() {
        refreshData();

        scheduler.scheduleAtFixedRate(this::refreshData,
                Constants.WEATHER_REFRESH_INTERVAL_MS,
                Constants.WEATHER_REFRESH_INTERVAL_MS,
                TimeUnit.MILLISECONDS);

        logger.info("WeatherService initialized and scheduled for periodic refresh.");
    }

    public void shutdown() {
        scheduler.shutdownNow();
        logger.info("WeatherService scheduler shutdown.");
    }

    public void addListener(Consumer<Map<String, WeatherInfo>> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<Map<String, WeatherInfo>> listener) {
        listeners.remove(listener);
    }

    public Map<String, WeatherInfo> getCurrentWeatherData() {
        return new HashMap<>(weatherDataByLocation);
    }

    private void refreshData() {
        logger.debug("Refreshing weather data...");
        try {
            // Define locations of interest in Sri Lanka (city names)
            List<String> locations = List.of("Colombo", "Galle", "Trincomalee", "Jaffna", "Kandy", "Hambantota");

            Map<String, WeatherInfo> newData = new HashMap<>();
            for (String location : locations) {
                WeatherInfo wInfo = fetchWeatherForLocation(location);
                if (wInfo != null) {
                    newData.put(location, wInfo);
                }
            }
            weatherDataByLocation = newData;
            notifyListeners();
            logger.info("Weather data refreshed successfully.");
        } catch (Exception e) {
            logger.error("Error refreshing weather data", e);
        }
    }

    private WeatherInfo fetchWeatherForLocation(String location) {
        try {
            String url = String.format(
                    "%sweather?q=%s&appid=%s&units=metric",
                    Constants.OPENWEATHERMAP_API_BASE_URL,
                    location,
                    Constants.OPENWEATHERMAP_API_KEY
            );

            JsonNode root = APIClient.getJson(url, null, Constants.API_TIMEOUT_MS);

            if (root == null || root.isEmpty()) return null;

            JsonNode main = root.get("main");
            JsonNode wind = root.get("wind");
            JsonNode weatherArr = root.get("weather");
            JsonNode alertsArr = root.has("alerts") ? root.get("alerts") : null;

            double temp = main != null ? main.path("temp").asDouble(0) : 0;
            double humidity = main != null ? main.path("humidity").asDouble(0) : 0;
            double windSpeed = wind != null ? wind.path("speed").asDouble(0) : 0;
            String weatherDesc = "";

            if (weatherArr != null && weatherArr.isArray() && weatherArr.size() > 0) {
                weatherDesc = weatherArr.get(0).path("description").asText("");
            }

            List<WeatherInfo.WeatherAlert> alerts = new ArrayList<>();
            if (alertsArr != null && alertsArr.isArray()) {
                for (JsonNode alertNode : alertsArr) {
                    String title = alertNode.path("event").asText("");
                    String desc = alertNode.path("description").asText("");
                    String severity = alertNode.path("severity").asText("Unknown");
                    if (!title.isEmpty()) {
                        alerts.add(new WeatherInfo.WeatherAlert(title, desc, severity));
                    }
                }
            }

            long dt = root.path("dt").asLong(System.currentTimeMillis() / 1000);
            LocalDateTime timestamp = LocalDateTime.ofInstant(Instant.ofEpochSecond(dt), ZoneId.systemDefault());

            return new WeatherInfo(location, temp, humidity, windSpeed, weatherDesc, alerts, timestamp);
        } catch (Exception e) {
            logger.error("Error fetching weather for location: " + location, e);
            return null;
        }
    }

    private void notifyListeners() {
        for (Consumer<Map<String, WeatherInfo>> listener : listeners) {
            try {
                listener.accept(getCurrentWeatherData());
            } catch (Exception ignored) {
            }
        }
    }
}
