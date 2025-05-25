package parking.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Alternative database configuration using properties file
 * (Optional - you can use MySQLConnectionAdapter instead)
 */
public class DatabaseConfig {
    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;
    private static String DRIVER;
    
    static {
        loadConfig();
    }
    
    /**
     * Load database configuration from properties file
     */
    private static void loadConfig() {
        try (InputStream input = DatabaseConfig.class.getResourceAsStream("/database/database.properties")) {
            Properties prop = new Properties();
            
            if (input != null) {
                prop.load(input);
                
                URL = prop.getProperty("db.url", "jdbc:mysql://localhost:3306/parking_db?serverTimezone=UTC");
                USERNAME = prop.getProperty("db.username", "root");
                PASSWORD = prop.getProperty("db.password", "password");
                DRIVER = prop.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            } else {
                // Use default values if properties file not found
                setDefaultValues();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load database configuration, using defaults");
            setDefaultValues();
        }
    }
    
    /**
     * Set default database configuration values
     */
    private static void setDefaultValues() {
        URL = "jdbc:mysql://localhost:3306/parking_db?serverTimezone=UTC";
        USERNAME = "root";
        PASSWORD = "password";
        DRIVER = "com.mysql.cj.jdbc.Driver";
    }
    
    /**
     * Get database connection
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found: " + DRIVER, e);
        }
    }
    
    /**
     * Test database connection
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    // Getters
    public static String getUrl() { return URL; }
    public static String getUsername() { return USERNAME; }
    public static String getPassword() { return PASSWORD; }
    public static String getDriver() { return DRIVER; }
    
    /**
     * Print current configuration (for debugging)
     */
    public static void printConfig() {
        System.out.println("Database Configuration:");
        System.out.println("URL: " + URL);
        System.out.println("Username: " + USERNAME);
        System.out.println("Driver: " + DRIVER);
        System.out.println("Connection test: " + (testConnection() ? "SUCCESS" : "FAILED"));
    }
}