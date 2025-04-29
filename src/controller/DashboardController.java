package src.controller;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import src.model.FishPrice;
import src.model.FishDistributionData;
import src.model.WeatherInfo;
import src.service.FishDistributionService;
import src.service.MarketPriceService;
import src.service.WeatherService;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @FXML
    private TableView<FishPrice> fishPriceTable;
    @FXML
    private TableColumn<FishPrice, String> speciesColumn;
    @FXML
    private TableColumn<FishPrice, Number> priceColumn;
    @FXML
    private TableColumn<FishPrice, String> locationColumn;
    @FXML
    private TableColumn<FishPrice, String> timestampColumn;

    @FXML
    private Label weatherLocationLabel;
    @FXML
    private Label weatherTempLabel;
    @FXML
    private Label weatherHumidityLabel;
    @FXML
    private Label weatherWindLabel;
    @FXML
    private Label weatherDescLabel;
    @FXML
    private Label weatherAlertLabel;

    @FXML
    private ComboBox<String> locationComboBox;
    @FXML
    private Button refreshButton;

    @FXML
    private WebView distributionMapView;

    private MarketPriceService marketPriceService;
    private WeatherService weatherService;
    private FishDistributionService fishDistributionService;

    private WebEngine webEngine;

    private final DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public DashboardController() {
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupLocationComboBox();
        webEngine = distributionMapView.getEngine();
        loadMapHtml();

        // Services will be set from MainApp after FXML load
    }

    public void setServices(MarketPriceService marketPriceService, WeatherService weatherService, FishDistributionService fishDistributionService) {
        this.marketPriceService = marketPriceService;
        this.weatherService = weatherService;
        this.fishDistributionService = fishDistributionService;

        this.marketPriceService.addListener(this::onFishPriceUpdated);
        this.weatherService.addListener(this::onWeatherUpdated);
        this.fishDistributionService.addListener(this::onFishDistributionUpdated);

        refreshAllData();
    }

    private void setupTableColumns() {
        speciesColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getSpeciesName()));
        priceColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getPricePerKg()));
        locationColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getLocation()));
        timestampColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getTimestamp().format(timestampFormatter)));
    }

    private void setupLocationComboBox() {
        locationComboBox.getItems().add("All Locations");
        locationComboBox.setValue("All Locations");
        locationComboBox.setOnAction(event -> filterFishPricesByLocation());
    }

    @FXML
    private void onRefreshClicked() {
        refreshAllData();
    }

    private void refreshAllData() {
        if (marketPriceService != null) {
            marketPriceService.addListener(this::onFishPriceUpdated);
            onFishPriceUpdated(marketPriceService.getCurrentPrices());
        }
        if (weatherService != null) {
            weatherService.addListener(this::onWeatherUpdated);
            onWeatherUpdated(weatherService.getCurrentWeatherData());
        }
        if (fishDistributionService != null) {
            fishDistributionService.addListener(this::onFishDistributionUpdated);
            onFishDistributionUpdated(fishDistributionService.getDistributionData());
        }
    }

    private void onFishPriceUpdated(Map<String, List<FishPrice>> data) {
        Platform.runLater(() -> {
            List<FishPrice> allPrices = data.values().stream().flatMap(Collection::stream).collect(Collectors.toList());

            // Update locations in combo box
            Set<String> locations = new TreeSet<>(data.keySet());
            if (!locations.isEmpty()) {
                locationComboBox.getItems().setAll("All Locations");
                locationComboBox.getItems().addAll(locations);
            }

            updateFishPriceTable(allPrices);
        });
    }

    private void updateFishPriceTable(List<FishPrice> prices) {
        String selectedLocation = locationComboBox.getValue();
        List<FishPrice> filtered;
        if (selectedLocation == null || "All Locations".equals(selectedLocation)) {
            filtered = prices;
        } else {
            filtered = prices.stream()
                    .filter(fp -> fp.getLocation().equalsIgnoreCase(selectedLocation))
                    .collect(Collectors.toList());
        }
        fishPriceTable.getItems().setAll(filtered);
    }

    private void filterFishPricesByLocation() {
        Map<String, List<FishPrice>> currentData = marketPriceService != null ? marketPriceService.getCurrentPrices() : Collections.emptyMap();
        List<FishPrice> allPrices = currentData.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        updateFishPriceTable(allPrices);
    }

    private void onWeatherUpdated(Map<String, WeatherInfo> weatherData) {
        Platform.runLater(() -> {
            if (weatherData.isEmpty()) {
                clearWeatherLabels();
                return;
            }
            String selectedLocation = locationComboBox.getValue();
            WeatherInfo selectedWeather;
            if (selectedLocation == null || "All Locations".equals(selectedLocation)) {
                // Pick first
                selectedWeather = weatherData.values().stream().findFirst().orElse(null);
            } else {
                selectedWeather = weatherData.get(selectedLocation);
            }
            if (selectedWeather == null) {
                clearWeatherLabels();
                return;
            }

            weatherLocationLabel.setText(selectedWeather.getLocation());
            weatherTempLabel.setText(String.format("%.1f °C", selectedWeather.getTemperature()));
            weatherHumidityLabel.setText(String.format("%.0f%%", selectedWeather.getHumidity()));
            weatherWindLabel.setText(String.format("%.1f m/s", selectedWeather.getWindSpeed()));
            weatherDescLabel.setText(selectedWeather.getWeatherDescription());

            if (selectedWeather.hasUnsafeAlert()) {
                WeatherInfo.WeatherAlert alert = selectedWeather.getAlerts().stream()
                        .filter(WeatherInfo.WeatherAlert::isSevere).findFirst().orElse(null);
                if (alert != null) {
                    weatherAlertLabel.setText("ALERT: " + alert.getTitle());
                    weatherAlertLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                } else {
                    weatherAlertLabel.setText("");
                }
            } else {
                weatherAlertLabel.setText("");
            }
        });
    }

    private void clearWeatherLabels() {
        weatherLocationLabel.setText("-");
        weatherTempLabel.setText("-");
        weatherHumidityLabel.setText("-");
        weatherWindLabel.setText("-");
        weatherDescLabel.setText("-");
        weatherAlertLabel.setText("");
    }

    private void onFishDistributionUpdated(List<FishDistributionData> distributionData) {
        Platform.runLater(() -> {
            if (distributionData == null || distributionData.isEmpty()) {
                loadMapHtml(); // reload empty map
                return;
            }

            // Build JavaScript markers addition code for the map
            StringBuilder sb = new StringBuilder();
            sb.append("clearMarkers();");
            distributionData.forEach(d -> {
                sb.append(String.format(Locale.US,
                        "addMarker(%f, %f, '%s', %.2f);",
                        d.getLatitude(), d.getLongitude(), escapeJS(d.getSpeciesIdentifier()), d.getDensityValue()));
            });
            webEngine.executeScript(sb.toString());
        });
    }

    private String escapeJS(String s) {
        if (s == null) return "";
        return s.replace("'", "\\'");
    }

    private void loadMapHtml() {
        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="utf-8" />
                  <title>Fish Distribution Map</title>
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.3/dist/leaflet.css" />
                  <style>
                    #map { height: 100%; }
                  </style>
                </head>
                <body>
                  <div id="map"></div>
                  <script src="https://unpkg.com/leaflet@1.9.3/dist/leaflet.js"></script>
                  <script>
                    const map = L.map('map').setView([7.8731, 80.7718], 7); // center Sri Lanka

                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                      maxZoom: 18,
                      attribution: '© OpenStreetMap'
                    }).addTo(map);

                    let markers = [];

                    function addMarker(lat, lng, species, density) {
                      const color = density > 50 ? 'red' : density > 20 ? 'orange' : 'green';
                      const marker = L.circleMarker([lat, lng], {
                        radius: Math.min(Math.max(density / 10, 5), 15),
                        color: color,
                        fillOpacity: 0.6
                      }).addTo(map);
                      marker.bindPopup(species + "<br>Density: " + density.toFixed(2));
                      markers.push(marker);
                    }

                    function clearMarkers() {
                      markers.forEach(m => {
                        map.removeLayer(m);
                      });
                      markers = [];
                    }
                  </script>
                </body>
                </html>
                """;

        webEngine.loadContent(html);
    }
}
