package src.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class FishDistributionData {
    private double latitude;
    private double longitude;
    private String speciesIdentifier;
    private double densityValue;
    private LocalDateTime timestamp;

    public FishDistributionData() {
    }

    public FishDistributionData(double latitude, double longitude, String speciesIdentifier, double densityValue, LocalDateTime timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.speciesIdentifier = speciesIdentifier;
        this.densityValue = densityValue;
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getSpeciesIdentifier() {
        return speciesIdentifier;
    }

    public void setSpeciesIdentifier(String speciesIdentifier) {
        this.speciesIdentifier = speciesIdentifier;
    }

    public double getDensityValue() {
        return densityValue;
    }

    public void setDensityValue(double densityValue) {
        this.densityValue = densityValue;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FishDistributionData)) return false;
        FishDistributionData that = (FishDistributionData) o;
        return Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0 &&
                Double.compare(that.densityValue, densityValue) == 0 &&
                Objects.equals(speciesIdentifier, that.speciesIdentifier) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, speciesIdentifier, densityValue, timestamp);
    }
}
