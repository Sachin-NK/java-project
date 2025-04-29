package src.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class FishPrice {
    private String speciesName;
    private double pricePerKg;
    private LocalDateTime timestamp;
    private String location;

    public FishPrice() {
    }

    public FishPrice(String speciesName, double pricePerKg, LocalDateTime timestamp, String location) {
        this.speciesName = speciesName;
        this.pricePerKg = pricePerKg;
        this.timestamp = timestamp;
        this.location = location;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }

    public double getPricePerKg() {
        return pricePerKg;
    }

    public void setPricePerKg(double pricePerKg) {
        this.pricePerKg = pricePerKg;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FishPrice)) return false;
        FishPrice fishPrice = (FishPrice) o;
        return Double.compare(fishPrice.pricePerKg, pricePerKg) == 0 &&
                Objects.equals(speciesName, fishPrice.speciesName) &&
                Objects.equals(timestamp, fishPrice.timestamp) &&
                Objects.equals(location, fishPrice.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(speciesName, pricePerKg, timestamp, location);
    }

    @Override
    public String toString() {
        return "FishPrice{" +
                "speciesName='" + speciesName + '\'' +
                ", pricePerKg=" + pricePerKg +
                ", timestamp=" + timestamp +
                ", location='" + location + '\'' +
                '}';
    }
}
