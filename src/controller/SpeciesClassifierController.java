package src.controller;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import src.model.SpeciesClassificationResult;
import src.service.SpeciesClassifierService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.CompletableFuture;

public class SpeciesClassifierController {

    private static final Logger logger = LoggerFactory.getLogger(SpeciesClassifierController.class);

    @FXML
    private Button uploadButton;

    @FXML
    private ImageView imagePreview;

    @FXML
    private TextArea classificationResultArea;

    @FXML
    private Button fetchDetailsButton;

    private SpeciesClassifierService speciesClassifierService;

    private SpeciesClassificationResult lastResult;

    private File selectedImageFile;

    public SpeciesClassifierController() {
    }

    @FXML
    public void initialize() {
        uploadButton.setOnAction(e -> onUploadClicked());
        fetchDetailsButton.setDisable(true);
        fetchDetailsButton.setOnAction(e -> onFetchDetailsClicked());
    }

    public void setSpeciesClassifierService(SpeciesClassifierService service) {
        this.speciesClassifierService = service;
    }

    private void onUploadClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Fish Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );
        File file = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            Image image = new Image(file.toURI().toString());
            imagePreview.setImage(image);

            classificationResultArea.clear();
            fetchDetailsButton.setDisable(true);

            classifyImage(file);
        }
    }

    private void classifyImage(File imageFile) {
        try {
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            if (bufferedImage == null) {
                classificationResultArea.setText("Unsupported image file.");
                return;
            }
            classificationResultArea.setText("Running classification...");
            CompletableFuture<SpeciesClassificationResult> future = speciesClassifierService.classifyImageAsync(bufferedImage);
            future.whenComplete((result, throwable) -> {
                if (throwable != null || result == null) {
                    Platform.runLater(() -> classificationResultArea.setText("Classification failed: " + throwable));
                } else {
                    lastResult = result;
                    Platform.runLater(() -> {
                        classificationResultArea.setText(formatResult(result));
                        fetchDetailsButton.setDisable(false);
                    });
                }
            });
        } catch (Exception e) {
            classificationResultArea.setText("Failed to process image: " + e.getMessage());
            logger.error("Exception processing image for classification", e);
        }
    }

    private String formatResult(SpeciesClassificationResult result) {
        return String.format("Species: %s%nConfidence: %.2f%%%nMore info: %s",
                result.getPredictedSpeciesName(),
                result.getConfidenceScore() * 100,
                result.getInfoLink());
    }

    private void onFetchDetailsClicked() {
        if (lastResult == null) return;
        String species = lastResult.getPredictedSpeciesName();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Species Details");
        alert.setHeaderText(species);

        // Normally fetch data from API or DB, here we simulate
        String details = getSpeciesDetails(species);

        alert.setContentText(details);
        alert.showAndWait();
    }

    private String getSpeciesDetails(String species) {
        switch (species.toLowerCase()) {
            case "sardine":
                return "Habitat: Coastal waters\nNutrition: High in omega-3\nMarket Relevance: Widely consumed locally.";
            case "tuna":
                return "Habitat: Open ocean\nNutrition: Rich in protein\nMarket Relevance: High export value.";
            case "shrimp":
                return "Habitat: Estuaries and coastal\nNutrition: Low fat, protein-rich\nMarket Relevance: Important export.";
            default:
                return "No detailed information available.";
        }
    }
}
