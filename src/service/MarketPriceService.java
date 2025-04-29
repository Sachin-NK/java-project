package src.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import src.model.FishPrice;
import src.util.APIClient;
import src.util.Constants;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class MarketPriceService {
    private static final Logger logger = LoggerFactory.getLogger(MarketPriceService.class);

    private final ScheduledExecutorService scheduler;
    private volatile Map<String, List<FishPrice>> priceDataByLocation;
    private final List<Consumer<Map<String, List<FishPrice>>>> listeners;

    public MarketPriceService() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "MarketPriceService"));
        this.priceDataByLocation = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
    }

    public void init() {
        refreshData();

        scheduler.scheduleAtFixedRate(this::refreshData,
                Constants.MARKET_PRICE_REFRESH_INTERVAL_MS,
                Constants.MARKET_PRICE_REFRESH_INTERVAL_MS,
                TimeUnit.MILLISECONDS);
        logger.info("MarketPriceService initialized and scheduled for periodic refresh.");
    }

    public void shutdown() {
        scheduler.shutdownNow();
        logger.info("MarketPriceService scheduler shutdown.");
    }

    public void addListener(Consumer<Map<String, List<FishPrice>>> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<Map<String, List<FishPrice>>> listener) {
        listeners.remove(listener);
    }

    public Map<String, List<FishPrice>> getCurrentPrices() {
        return new HashMap<>(priceDataByLocation);
    }

    private void refreshData() {
        logger.debug("Refreshing fish market price data...");
        try {
            Map<String, List<FishPrice>> aggregatedPrices = new HashMap<>();
            List<FishPrice> ceyfishPrices = fetchFromCeyfish();
            List<FishPrice> govPrices = fetchFromGovernment();

            for (FishPrice fp : ceyfishPrices) {
                aggregatedPrices.computeIfAbsent(fp.getLocation(), k -> new ArrayList<>()).add(fp);
            }
            for (FishPrice fp : govPrices) {
                aggregatedPrices.computeIfAbsent(fp.getLocation(), k -> new ArrayList<>()).add(fp);
            }

            priceDataByLocation = aggregatedPrices;
            notifyListeners();
            logger.info("Fish market price data refreshed successfully.");
        } catch (Exception e) {
            logger.error("Error refreshing fish market prices", e);
        }
    }

    private List<FishPrice> fetchFromCeyfish() {
        try {
            String url = Constants.CEYFISH_API_BASE_URL + "prices?api_key=" + Constants.CEYFISH_API_KEY;

            JsonNode root = APIClient.getJson(url, null, Constants.API_TIMEOUT_MS);
            List<FishPrice> list = new ArrayList<>();

            if (root.has("prices") && root.get("prices").isArray()) {
                for (JsonNode obj : root.get("prices")) {
                    String species = obj.path("species_name").asText("");
                    double price = obj.path("price_per_kg").asDouble(0);
                    String location = obj.path("location").asText("");
                    long timestampEpoch = obj.path("timestamp").asLong(System.currentTimeMillis() / 1000);
                    LocalDateTime timestamp = LocalDateTime.ofEpochSecond(timestampEpoch, 0, ZoneOffset.UTC);

                    if (!species.isEmpty() && !location.isEmpty() && price > 0) {
                        list.add(new FishPrice(species, price, timestamp, location));
                    }
                }
            }
            return list;
        } catch (Exception e) {
            logger.error("Error fetching data from Ceyfish API", e);
            return Collections.emptyList();
        }
    }

    private List<FishPrice> fetchFromGovernment() {
        try {
            String url = Constants.GOVERNMENT_FISHERIES_API_BASE_URL + "fishprices?api_key=" + Constants.FISHERIES_GOV_API_KEY;
            JsonNode root = APIClient.getJson(url, null, Constants.API_TIMEOUT_MS);
            List<FishPrice> list = new ArrayList<>();

            if (root.has("data") && root.get("data").isArray()) {
                for (JsonNode obj : root.get("data")) {
                    String species = obj.path("species").asText("");
                    double price = obj.path("price").asDouble(0);
                    String location = obj.path("area").asText("");
                    String timeStr = obj.path("recorded_at").asText("");
                    LocalDateTime timestamp;
                    try {
                        timestamp = LocalDateTime.parse(timeStr);
                    } catch (Exception e) {
                        timestamp = LocalDateTime.now();
                    }

                    if (!species.isEmpty() && !location.isEmpty() && price > 0) {
                        list.add(new FishPrice(species, price, timestamp, location));
                    }
                }
            }
            return list;
        } catch (Exception e) {
            logger.error("Error fetching data from Government Fisheries API", e);
            return Collections.emptyList();
        }
    }

    private void notifyListeners() {
        for (Consumer<Map<String, List<FishPrice>>> listener : listeners) {
            try {
                listener.accept(getCurrentPrices());
            } catch (Exception ignored) {
            }
        }
    }
}
