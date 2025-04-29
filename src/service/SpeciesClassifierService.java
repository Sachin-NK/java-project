package src.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import src.model.SpeciesClassificationResult;

import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

public class SpeciesClassifierService {
    private static final Logger logger = LoggerFactory.getLogger(SpeciesClassifierService.class);

    public void init() {
        logger.info("SpeciesClassifierService initialized.");
    }

    public void shutdown() {
        logger.info("SpeciesClassifierService shutdown completed.");
    }

    public CompletableFuture<SpeciesClassificationResult> classifyImageAsync(BufferedImage image) {
        return CompletableFuture.supplyAsync(() -> classifyImage(image));
    }

    private SpeciesClassificationResult classifyImage(BufferedImage image) {
        try {
            // For demonstration purposes, returning a default species
            // You can implement your own classification logic here
            String speciesName = getSpeciesNameByIndex(0);
            double confidence = 0.95; // Example confidence score
            return new SpeciesClassificationResult(speciesName, confidence, getSpeciesInfoLink(speciesName));
        } catch (Exception e) {
            logger.error("Exception during image classification", e);
            return null;
        }
    }

    private String getSpeciesNameByIndex(int index) {
        // Dummy mapping for demo, should be replaced by actual mapping
        switch (index) {
            case 0:
                return "Sardine";
            case 1:
                return "Tuna";
            case 2:
                return "Shrimp";
            default:
                return "Unknown Species";
        }
    }

    private String getSpeciesInfoLink(String speciesName) {
        return "https://en.wikipedia.org/wiki/" + speciesName.replace(" ", "_");
    }
}