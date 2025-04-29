package src.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class WeatherInfo {
    private String location;
    private double temperature;
    private double humidity;
    private double windSpeed;
    private String weatherDescription;
    private List<WeatherAlert> alerts;
    private LocalDateTime timestamp;

    public WeatherInfo() {
    }

    public WeatherInfo(String location, double temperature, double humidity, double windSpeed, String weatherDescription, List<WeatherAlert> alerts, LocalDateTime timestamp) {
        this.location = location;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.weatherDescription = weatherDescription;
        this.alerts = alerts;
        this.timestamp = timestamp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public void setWeatherDescription(String weatherDescription) {
        this.weatherDescription = weatherDescription;
    }

    public List<WeatherAlert> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<WeatherAlert> alerts) {
        this.alerts = alerts;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean hasUnsafeAlert() {
        if (alerts == null) return false;
        return alerts.stream().anyMatch(WeatherAlert::isSevere);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WeatherInfo)) return false;
        WeatherInfo that = (WeatherInfo) o;
        return Double.compare(that.temperature, temperature) == 0 &&
                Double.compare(that.humidity, humidity) == 0 &&
                Double.compare(that.windSpeed, windSpeed) == 0 &&
                Objects.equals(location, that.location) &&
                Objects.equals(weatherDescription, that.weatherDescription) &&
                Objects.equals(alerts, that.alerts) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, temperature, humidity, windSpeed, weatherDescription, alerts, timestamp);
    }

    public static class WeatherAlert {
        private String title;
        private String description;
        private String severity; // e.g., "Severe", "Moderate", "Minor", "Unknown"

        public WeatherAlert() {
        }

        public WeatherAlert(String title, String description, String severity) {
            this.title = title;
            this.description = description;
            this.severity = severity;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public boolean isSevere() {
            return "Severe".equalsIgnoreCase(severity) || "Warning".equalsIgnoreCase(severity);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof WeatherAlert)) return false;
            WeatherAlert that = (WeatherAlert) o;
            return Objects.equals(title, that.title) &&
                    Objects.equals(description, that.description) &&
                    Objects.equals(severity, that.severity);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, description, severity);
        }
    }
}
