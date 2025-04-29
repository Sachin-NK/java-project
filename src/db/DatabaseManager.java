package src.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import src.util.Constants;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    private static volatile DatabaseManager instance;
    private HikariDataSource dataSource;

    private DatabaseManager() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(Constants.DB_URL);
            config.setUsername(Constants.DB_USER);
            config.setPassword(Constants.DB_PASSWORD);
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(10_000);
            config.setLeakDetectionThreshold(60_000);
            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            logger.error("Failed to initialize HikariCP DataSource", e);
            throw new RuntimeException(e);
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void initializeSchema() {
        // Create tables if not exist with simple SQL schema
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  username VARCHAR(50) NOT NULL UNIQUE,
                  password_hash VARCHAR(255) NOT NULL,
                  display_name VARCHAR(100),
                  contact_info VARCHAR(255),
                  permissions VARCHAR(255),
                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

            // chat_messages table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS chat_messages (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  sender_username VARCHAR(50) NOT NULL,
                  message TEXT NOT NULL,
                  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  message_type VARCHAR(20) NOT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

            // fish_price_history table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS fish_price_history (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  species_name VARCHAR(100) NOT NULL,
                  price_per_kg DECIMAL(10, 2) NOT NULL,
                  location VARCHAR(100) NOT NULL,
                  recorded_at TIMESTAMP NOT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

            // weather_logs table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS weather_logs (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  location VARCHAR(100) NOT NULL,
                  temperature DOUBLE,
                  humidity DOUBLE,
                  wind_speed DOUBLE,
                  weather_description VARCHAR(255),
                  alert VARCHAR(255),
                  recorded_at TIMESTAMP NOT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

            // species_data table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS species_data (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  species_name VARCHAR(100) UNIQUE,
                  habitat_info TEXT,
                  nutritional_facts TEXT,
                  market_relevance TEXT,
                  last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

        } catch (SQLException e) {
            logger.error("Exception initializing schema", e);
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("HikariCP DataSource closed.");
        }
    }
}
