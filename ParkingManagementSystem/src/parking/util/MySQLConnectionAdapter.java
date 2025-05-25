package parking.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * MySQL Connection utility adapted for Parking Management System
 * Based on the original mysqlConnection.java but enhanced for parking operations
 */
public class MySQLConnectionAdapter {
    
    // Database connection parameters - MODIFY THESE FOR YOUR SETUP
    private static final String DB_URL = "jdbc:mysql://localhost:3306/parking_db?serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Aa123456"; // CHANGE THIS TO YOUR MYSQL PASSWORD!
    
    private static Connection connection = null;
    
    /**
     * Initialize database connection
     */
    public static boolean initializeConnection() {
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            System.out.println("MySQL Driver definition succeeded");
            
            // Establish connection
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("MySQL connection succeeded");
            
            // Test the connection with a simple query
            if (testConnection()) {
                System.out.println("Database connection test passed");
                return true;
            } else {
                System.out.println("Database connection test failed");
                return false;
            }
            
        } catch (Exception ex) {
            System.err.println("MySQL connection failed: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get the database connection
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            if (!initializeConnection()) {
                throw new SQLException("Could not establish database connection");
            }
        }
        return connection;
    }
    
    /**
     * Test database connection with parking tables
     */
    public static boolean testConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                return false;
            }
            
            // Test if parking tables exist
            String testQuery = "SELECT COUNT(*) FROM parking_orders LIMIT 1";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(testQuery)) {
                
                System.out.println("Parking tables accessible");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            
            // If tables don't exist, try to create them
            return createParkingTables();
        }
    }
    
    /**
     * Create parking tables if they don't exist
     */
    private static boolean createParkingTables() {
        try {
            System.out.println("Attempting to create parking tables...");
            
            // Create parking_orders table
            String createOrdersTable = """
                CREATE TABLE IF NOT EXISTS parking_orders (
                    order_id INT AUTO_INCREMENT PRIMARY KEY,
                    parking_spot_number INT NOT NULL,
                    subscriber_id VARCHAR(50) NOT NULL,
                    date_of_parking DATE NOT NULL,
                    date_placing_order DATE NOT NULL,
                    time_of_car_deposit TIME NOT NULL,
                    time_of_retrieval_time TIME,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    
                    INDEX idx_spot_date (parking_spot_number, date_of_parking),
                    INDEX idx_subscriber (subscriber_id),
                    
                    CHECK (parking_spot_number BETWEEN 1 AND 100)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;
            
            // Create parking_availability table
            String createAvailabilityTable = """
                CREATE TABLE IF NOT EXISTS parking_availability (
                    availability_date DATE,
                    time_slot TIME,
                    occupied_spots INT DEFAULT 0,
                    free_spots INT DEFAULT 100,
                    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    
                    PRIMARY KEY (availability_date, time_slot),
                    
                    CHECK (occupied_spots >= 0 AND occupied_spots <= 100),
                    CHECK (free_spots >= 0 AND free_spots <= 100),
                    CHECK (occupied_spots + free_spots = 100)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;
            
            // Create spot_availability table
            String createSpotAvailabilityTable = """
                CREATE TABLE IF NOT EXISTS spot_availability (
                    availability_date DATE,
                    time_slot TIME,
                    parking_spot_number INT,
                    is_occupied BOOLEAN DEFAULT FALSE,
                    reserved_by VARCHAR(50) DEFAULT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    
                    PRIMARY KEY (availability_date, time_slot, parking_spot_number),
                    
                    INDEX idx_date_spot (availability_date, parking_spot_number),
                    INDEX idx_occupied (availability_date, time_slot, is_occupied),
                    
                    CHECK (parking_spot_number BETWEEN 1 AND 100)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;
            
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(createOrdersTable);
                System.out.println("Created parking_orders table");
                
                stmt.executeUpdate(createAvailabilityTable);
                System.out.println("Created parking_availability table");
                
                stmt.executeUpdate(createSpotAvailabilityTable);
                System.out.println("Created spot_availability table");
                
                // Initialize with some basic availability data
                initializeBasicAvailability();
                
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Failed to create parking tables: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Initialize basic availability data for testing
     */
    private static void initializeBasicAvailability() {
        try {
            System.out.println("Initializing basic availability data...");
            
            // Check if data already exists
            String checkQuery = "SELECT COUNT(*) FROM parking_availability";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(checkQuery)) {
                
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Availability data already exists");
                    return;
                }
            }
            
            // Insert basic availability for today and tomorrow
            String insertAvailability = """
                INSERT INTO parking_availability (availability_date, time_slot, occupied_spots, free_spots)
                VALUES (?, ?, 0, 100)
                """;
            
            String insertSpotAvailability = """
                INSERT INTO spot_availability (availability_date, time_slot, parking_spot_number, is_occupied)
                VALUES (?, ?, ?, FALSE)
                """;
            
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate tomorrow = today.plusDays(1);
            
            try (PreparedStatement availStmt = connection.prepareStatement(insertAvailability);
                 PreparedStatement spotStmt = connection.prepareStatement(insertSpotAvailability)) {
                
                for (java.time.LocalDate date : new java.time.LocalDate[]{today, tomorrow}) {
                    for (int hour = 0; hour < 24; hour++) {
                        for (int minute = 0; minute < 60; minute += 15) {
                            java.time.LocalTime timeSlot = java.time.LocalTime.of(hour, minute);
                            
                            // Insert general availability
                            availStmt.setDate(1, java.sql.Date.valueOf(date));
                            availStmt.setTime(2, java.sql.Time.valueOf(timeSlot));
                            availStmt.executeUpdate();
                            
                            // Insert spot-specific availability for all 100 spots
                            for (int spot = 1; spot <= 100; spot++) {
                                spotStmt.setDate(1, java.sql.Date.valueOf(date));
                                spotStmt.setTime(2, java.sql.Time.valueOf(timeSlot));
                                spotStmt.setInt(3, spot);
                                spotStmt.executeUpdate();
                            }
                        }
                    }
                }
                
                System.out.println("Basic availability data initialized");
            }
            
        } catch (SQLException e) {
            System.err.println("Failed to initialize availability data: " + e.getMessage());
        }
    }
    
    /**
     * Print parking orders for debugging
     */
    public static void printParkingOrders() {
        try {
            String query = "SELECT * FROM parking_orders ORDER BY created_at DESC LIMIT 10";
            
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                
                System.out.println("\n=== Recent Parking Orders ===");
                while (rs.next()) {
                    System.out.printf("Order #%d: Spot #%d, Customer: %s, Date: %s, Time: %s-%s%n",
                                    rs.getInt("order_id"),
                                    rs.getInt("parking_spot_number"),
                                    rs.getString("subscriber_id"),
                                    rs.getDate("date_of_parking"),
                                    rs.getTime("time_of_car_deposit"),
                                    rs.getTime("time_of_retrieval_time"));
                }
                System.out.println("=========================");
            }
            
        } catch (SQLException e) {
            System.err.println("Error printing parking orders: " + e.getMessage());
        }
    }
    
    /**
     * Get current availability summary
     */
    public static void printAvailabilitySummary() {
        try {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalTime currentTime = java.time.LocalTime.now();
            
            // Round to nearest 15-minute slot
            int minutes = currentTime.getMinute();
            int roundedMinutes = (minutes / 15) * 15;
            currentTime = java.time.LocalTime.of(currentTime.getHour(), roundedMinutes);
            
            String query = """
                SELECT 
                    COUNT(*) as total_spots,
                    SUM(CASE WHEN is_occupied = FALSE THEN 1 ELSE 0 END) as free_spots,
                    SUM(CASE WHEN is_occupied = TRUE THEN 1 ELSE 0 END) as occupied_spots
                FROM spot_availability 
                WHERE availability_date = ? AND time_slot = ?
                """;
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setDate(1, java.sql.Date.valueOf(today));
                stmt.setTime(2, java.sql.Time.valueOf(currentTime));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int total = rs.getInt("total_spots");
                        int free = rs.getInt("free_spots");
                        int occupied = rs.getInt("occupied_spots");
                        double rate = total > 0 ? (double) occupied / total * 100 : 0;
                        
                        System.out.println("\n=== Parking Availability Summary ===");
                        System.out.printf("Time: %s%n", currentTime);
                        System.out.printf("Total spots: %d%n", total);
                        System.out.printf("Available: %d%n", free);
                        System.out.printf("Occupied: %d%n", occupied);
                        System.out.printf("Occupancy rate: %.1f%%%n", rate);
                        System.out.println("================================");
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting availability summary: " + e.getMessage());
        }
    }
    
    /**
     * Close database connection
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
    
    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        System.out.println("=== MySQL Parking Database Test ===");
        
        if (initializeConnection()) {
            System.out.println("✓ Database connection successful");
            
            printAvailabilitySummary();
            printParkingOrders();
            
            closeConnection();
        } else {
            System.out.println("✗ Database connection failed");
        }
    }
}