package parking.service;

import parking.model.*;
import parking.util.TimeUtils;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for optimal parking spot assignment
 * FIXED VERSION - All methods properly implemented
 */
public class ParkingSpotAssigner {
    private Connection connection;
    
    public ParkingSpotAssigner(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Assigns the optimal parking spot based on:
     * 1. Longest available duration first
     * 2. Smallest parking spot number (1-100) within that duration
     */
    public ParkingAssignment assignOptimalSpot(LocalDate date, LocalTime startTime) {
        try {
            List<SpotAvailability> availableSpots = getAvailableSpotsFromStartTime(date, startTime);
            
            if (availableSpots.isEmpty()) {
                return null;
            }
            
            // Group spots by their maximum continuous duration
            Map<Double, List<SpotAvailability>> spotsByDuration = availableSpots.stream()
                .collect(Collectors.groupingBy(SpotAvailability::getMaxDurationHours));
            
            // Find the longest duration available
            double longestDuration = spotsByDuration.keySet().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);
            
            // Cap at 4 hours maximum
            longestDuration = Math.min(longestDuration, 4.0);
            
            // Get all spots with the longest duration
            List<SpotAvailability> longestDurationSpots = spotsByDuration.get(longestDuration);
            
            // Sort by parking spot number (1, 2, 3, 4... smallest first)
            SpotAvailability optimalSpot = longestDurationSpots.stream()
                .sorted(Comparator.comparing(SpotAvailability::getParkingSpotNumber))
                .findFirst()
                .orElse(null);
            
            if (optimalSpot != null) {
                LocalTime endTime = startTime.plusMinutes((long)(longestDuration * 60));
                
                return new ParkingAssignment(
                    optimalSpot.getParkingSpotNumber(),
                    date,
                    startTime,
                    endTime,
                    longestDuration
                );
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get available spots (1-100) and their maximum duration from a start time
     */
    private List<SpotAvailability> getAvailableSpotsFromStartTime(LocalDate date, LocalTime startTime) throws SQLException {
        List<SpotAvailability> spots = new ArrayList<>();
        
        String query = """
            SELECT parking_spot_number, COUNT(*) * 0.25 as duration_hours
            FROM spot_availability 
            WHERE availability_date = ? 
              AND time_slot >= ? 
              AND time_slot < ADDTIME(?, '04:00:00')
              AND is_occupied = FALSE
              AND parking_spot_number BETWEEN 1 AND 100
            GROUP BY parking_spot_number
            HAVING duration_hours >= 0.25
            ORDER BY duration_hours DESC, parking_spot_number ASC
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(date));
            stmt.setTime(2, Time.valueOf(startTime));
            stmt.setTime(3, Time.valueOf(startTime));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int spotNumber = rs.getInt("parking_spot_number");
                    double duration = Math.min(rs.getDouble("duration_hours"), 4.0);
                    LocalTime until = startTime.plusMinutes((long)(duration * 60));
                    
                    spots.add(new SpotAvailability(spotNumber, duration, until));
                }
            }
        }
        
        return spots;
    }
}