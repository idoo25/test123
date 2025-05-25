package parking.service;

import parking.model.*;
import parking.util.TimeUtils;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Service for handling parking availability queries
 * FIXED VERSION - Date import ambiguity resolved
 */
public class ParkingAvailabilityService {
    private Connection connection;
    
    public ParkingAvailabilityService(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Get available time frames for a specific date
     */
    public List<OptimalTimeFrame> getAvailableTimeFrames(LocalDate date) {
        List<OptimalTimeFrame> timeFrames = new ArrayList<>();
        
        try {
            String query = """
                SELECT 
                    time_slot,
                    free_spots,
                    occupied_spots
                FROM parking_availability 
                WHERE availability_date = ? 
                  AND free_spots > 0
                ORDER BY time_slot
                """;
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setDate(1, java.sql.Date.valueOf(date)); // FIXED: Use java.sql.Date explicitly
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        LocalTime startTime = rs.getTime("time_slot").toLocalTime();
                        int freeSpots = rs.getInt("free_spots");
                        
                        // Calculate available duration from this time slot
                        double duration = calculateAvailableDuration(date, startTime);
                        if (duration >= 0.25) { // At least 15 minutes
                            LocalTime endTime = startTime.plusMinutes((long)(duration * 60));
                            
                            // Assign optimal spot for this time frame
                            int assignedSpot = findOptimalSpot(date, startTime, endTime);
                            
                            OptimalTimeFrame timeFrame = new OptimalTimeFrame(
                                date, startTime, endTime, duration, freeSpots, assignedSpot
                            );
                            timeFrames.add(timeFrame);
                        }
                    }
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return timeFrames;
    }
    
    /**
     * Calculate how long parking is available from a start time
     */
    private double calculateAvailableDuration(LocalDate date, LocalTime startTime) {
        try {
            String query = """
                SELECT COUNT(*) * 0.25 as duration_hours
                FROM parking_availability 
                WHERE availability_date = ? 
                  AND time_slot >= ? 
                  AND time_slot < ADDTIME(?, '04:00:00')
                  AND free_spots > 0
                """;
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setDate(1, java.sql.Date.valueOf(date)); // FIXED: Use java.sql.Date explicitly
                stmt.setTime(2, java.sql.Time.valueOf(startTime));
                stmt.setTime(3, java.sql.Time.valueOf(startTime));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Math.min(rs.getDouble("duration_hours"), 4.0); // Max 4 hours
                    }
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0.0;
    }
    
    /**
     * Find optimal spot for a time period
     */
    private int findOptimalSpot(LocalDate date, LocalTime startTime, LocalTime endTime) {
        try {
            String query = """
                SELECT parking_spot_number
                FROM spot_availability 
                WHERE availability_date = ? 
                  AND time_slot >= ? 
                  AND time_slot < ?
                  AND is_occupied = FALSE
                GROUP BY parking_spot_number
                HAVING COUNT(*) = ?
                ORDER BY parking_spot_number
                LIMIT 1
                """;
            
            long slots = java.time.Duration.between(startTime, endTime).toMinutes() / 15;
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setDate(1, java.sql.Date.valueOf(date)); // FIXED: Use java.sql.Date explicitly
                stmt.setTime(2, java.sql.Time.valueOf(startTime));
                stmt.setTime(3, java.sql.Time.valueOf(endTime));
                stmt.setLong(4, slots);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("parking_spot_number");
                    }
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return (int)(Math.random() * 100) + 1; // Fallback random spot
    }
}