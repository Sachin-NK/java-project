package src.model;

import java.util.Objects;

public class SpeciesClassificationResult {
    private String predictedSpeciesName;
    private double confidenceScore;
    private String infoLink;

    public SpeciesClassificationResult() {
    }

    public SpeciesClassificationResult(String predictedSpeciesName, double confidenceScore, String infoLink) {
        this.predictedSpeciesName = predictedSpeciesName;
        this.confidenceScore = confidenceScore;
        this.infoLink = infoLink;
    }

    public String getPredictedSpeciesName() {
        return predictedSpeciesName;
    }

    public void setPredictedSpeciesName(String predictedSpeciesName) {
        this.predictedSpeciesName = predictedSpeciesName;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getInfoLink() {
        return infoLink;
    }

    public void setInfoLink(String infoLink) {
        this.infoLink = infoLink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpeciesClassificationResult)) return false;
        SpeciesClassificationResult that = (SpeciesClassificationResult) o;
        return Double.compare(that.confidenceScore, confidenceScore) == 0 &&
                Objects.equals(predictedSpeciesName, that.predictedSpeciesName) &&
                Objects.equals(infoLink, that.infoLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(predictedSpeciesName, confidenceScore, infoLink);
    }
}
