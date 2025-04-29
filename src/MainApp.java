package src;

import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import src.db.DatabaseManager;
import src.service.*;
import src.service.ChatService;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class MainApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    private MarketPriceService marketPriceService;
    private WeatherService weatherService;
    private FishDistributionService fishDistributionService;
    private ChatService chatService;
    private ChatbotService chatbotService;
    private SpeciesClassifierService speciesClassifierService;

    private DatabaseManager databaseManager;

    @Override
    public void start(Stage primaryStage) {
        logger.info("Application starting...");

        setupDatabase();

        loadServicesAsync().thenAccept(v -> {
            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/MainLayout.fxml"));
                    Scene scene = new Scene(loader.load());
                    scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

                    primaryStage.setTitle("Smart Fisheries Management System");
                    primaryStage.setScene(scene);
                    primaryStage.getIcons().add(new Image("https://upload.wikimedia.org/wikipedia/commons/thumb/f/f6/Fish_icon.svg/1024px-Fish_icon.svg.png"));
                    primaryStage.setMinWidth(1024);
                    primaryStage.setMinHeight(768);
                    primaryStage.show();
                } catch (IOException e) {
                    logger.error("Failed to load main layout FXML", e);
                    Platform.exit();
                }
            });
        }).exceptionally(ex -> {
            logger.error("Failed to initialize services", ex);
            Platform.exit();
            return null;
        });
    }

    private void setupDatabase() {
        try {
            databaseManager = DatabaseManager.getInstance();
            databaseManager.initializeSchema();
            logger.info("Database initialized successfully.");
        } catch (Exception e) {
            logger.error("Database initialization failed", e);
            Platform.exit();
        }
    }

    private CompletableFuture<Void> loadServicesAsync() {
        return CompletableFuture.runAsync(() -> {
            marketPriceService = new MarketPriceService();
            weatherService = new WeatherService();
            fishDistributionService = new FishDistributionService();
            chatbotService = new ChatbotService();
            speciesClassifierService = new SpeciesClassifierService();
            chatService = new ChatService(databaseManager); // needs DatabaseManager for persistence

            marketPriceService.init();
            weatherService.init();
            fishDistributionService.init();
            chatService.init();
            chatbotService.init();
            speciesClassifierService.init();
            logger.info("All services initialized.");
        });
    }

    @Override
    public void stop() {
        logger.info("Application stopping...");
        if (chatService != null) chatService.shutdown();
        if (marketPriceService != null) marketPriceService.shutdown();
        if (weatherService != null) weatherService.shutdown();
        if (fishDistributionService != null) fishDistributionService.shutdown();
        if (speciesClassifierService != null) speciesClassifierService.shutdown();
        if (databaseManager != null) databaseManager.shutdown();
        logger.info("Resources cleaned up.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
