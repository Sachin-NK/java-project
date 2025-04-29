package src.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import src.model.FishDistributionData;
import src.util.APIClient;
import src.util.Constants;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;  // Add this import

public class FishDistributionService {
    private static final Logger logger = LoggerFactory.getLogger(FishDistributionService.class);

    private ScheduledExecutorService scheduler;
    private volatile List<FishDistributionData> distributionData;
    private final List<Consumer<List<FishDistributionData>>> listeners;

    public FishDistributionService() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "FishDistributionService"));
        this.distributionData = Collections.synchronizedList(new ArrayList<>());
        this.listeners = new CopyOnWriteArrayList<>();
    }

    public void init() {
        refreshData();

        scheduler.scheduleAtFixedRate(this::refreshData,
                Constants.FISH_DISTRIBUTION_REFRESH_INTERVAL_MS,
                Constants.FISH_DISTRIBUTION_REFRESH_INTERVAL_MS,
                TimeUnit.MILLISECONDS);

        logger.info("FishDistributionService initialized and scheduled for periodic refresh.");
    }

    public void shutdown() {
        scheduler.shutdownNow();
        logger.info("FishDistributionService scheduler shutdown.");
    }

    public void addListener(Consumer<List<FishDistributionData>> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<List<FishDistributionData>> listener) {
        listeners.remove(listener);
    }

    public List<FishDistributionData> getDistributionData() {
        return new ArrayList<>(distributionData);
    }

    private void refreshData() {
        logger.debug("Refreshing fish distribution data...");
        try {
            List<FishDistributionData> newData = fetchFishDistributionData();
            if (newData != null) {
                distributionData = newData;
                notifyListeners();
                logger.info("Fish distribution data refreshed.");
            }
        } catch (Exception e) {
            logger.error("Error refreshing fish distribution data", e);
        }
    }

    private List<FishDistributionData> fetchFishDistributionData() {
        try {
            String url = Constants.FAO_FISHERIES_DATA_URL; // Placeholder; replace with actual if available

            JsonNode root = APIClient.getJson(url, null, Constants.API_TIMEOUT_MS);
            if (root == null || root.isEmpty()) return Collections.emptyList();

            List<FishDistributionData> result = new ArrayList<>();

            if (root.isArray()) {
                for (JsonNode obj : root) {
                    double lat = obj.path("latitude").asDouble(Double.NaN);
                    double lng = obj.path("longitude").asDouble(Double.NaN);
                    String speciesId = obj.path("species_identifier").asText("");
                    double density = obj.path("density").asDouble(0);
                    long ts = obj.path("timestamp_epoch").asLong(System.currentTimeMillis() / 1000);

                    if (!Double.isNaN(lat) && !Double.isNaN(lng) && !speciesId.isEmpty()) {
                        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(ts, 0, java.time.ZoneOffset.UTC);
                        result.add(new FishDistributionData(lat, lng, speciesId, density, dateTime));
                    }
                }
            }

            return result;
        } catch (Exception e) {
            logger.error("Error fetching fish distribution data", e);
            return Collections.emptyList();
        }
    }

    private void notifyListeners() {
        for (Consumer<List<FishDistributionData>> listener : listeners) {
            try {
                listener.accept(getDistributionData());
            } catch (Exception ignored) {
            }
        }
    }
}