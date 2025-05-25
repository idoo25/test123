package parking.service;

import parking.model.CurrentSpotAvailability;
import java.io.Serializable;

/**
 * Result class for Park Now operations
 */
public class ParkNowResult implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean available;
    private CurrentSpotAvailability assignedSpot;
    private String message;
    private int totalAvailableSpots;
    
    public ParkNowResult(boolean available, CurrentSpotAvailability assignedSpot, 
                        String message, int totalAvailableSpots) {
        this.available = available;
        this.assignedSpot = assignedSpot;
        this.message = message;
        this.totalAvailableSpots = totalAvailableSpots;
    }
    
    public static ParkNowResult success(CurrentSpotAvailability assignedSpot, int totalAvailable) {
        return new ParkNowResult(true, assignedSpot, 
            String.format("Best spot: #%d (%.1f hours) - %d total spots available", 
                         assignedSpot.getSpotNumber(), assignedSpot.getDurationHours(), totalAvailable),
            totalAvailable);
    }
    
    public static ParkNowResult failure(String reason) {
        return new ParkNowResult(false, null, reason, 0);
    }
    
    public static ParkNowResult noAvailability() {
        return new ParkNowResult(false, null, "No parking spots available right now", 0);
    }
    
    // Getters
    public boolean isAvailable() { return available; }
    public CurrentSpotAvailability getAssignedSpot() { return assignedSpot; }
    public String getMessage() { return message; }
    public int getTotalAvailableSpots() { return totalAvailableSpots; }
}