package parking.model;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Serializable version of ParkingAvailabilitySummary
 */
public class ParkingAvailabilitySummary implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int totalSpots;
    private int freeSpots;
    private int occupiedSpots;
    private LocalTime asOfTime;
    
    public ParkingAvailabilitySummary(int totalSpots, int freeSpots, int occupiedSpots, LocalTime asOfTime) {
        this.totalSpots = totalSpots;
        this.freeSpots = freeSpots;
        this.occupiedSpots = occupiedSpots;
        this.asOfTime = asOfTime;
    }
    
    // Getters
    public int getTotalSpots() { return totalSpots; }
    public int getFreeSpots() { return freeSpots; }
    public int getOccupiedSpots() { return occupiedSpots; }
    public LocalTime getAsOfTime() { return asOfTime; }
    
    // Setters
    public void setTotalSpots(int totalSpots) { this.totalSpots = totalSpots; }
    public void setFreeSpots(int freeSpots) { this.freeSpots = freeSpots; }
    public void setOccupiedSpots(int occupiedSpots) { this.occupiedSpots = occupiedSpots; }
    public void setAsOfTime(LocalTime asOfTime) { this.asOfTime = asOfTime; }
    
    /**
     * Calculate occupancy rate as percentage
     */
    public double getOccupancyRate() {
        return totalSpots > 0 ? (double) occupiedSpots / totalSpots * 100 : 0;
    }
    
    /**
     * Calculate availability rate as percentage
     */
    public double getAvailabilityRate() {
        return totalSpots > 0 ? (double) freeSpots / totalSpots * 100 : 0;
    }
    
    /**
     * Check if parking lot is full
     */
    public boolean isFull() {
        return freeSpots == 0;
    }
    
    /**
     * Check if parking lot is nearly full (less than 10% available)
     */
    public boolean isNearlyFull() {
        return getAvailabilityRate() < 10.0;
    }
    
    /**
     * Check if parking lot is nearly empty (less than 10% occupied)
     */
    public boolean isNearlyEmpty() {
        return getOccupancyRate() < 10.0;
    }
    
    /**
     * Get status description
     */
    public String getStatusDescription() {
        if (isFull()) {
            return "Parking lot is full";
        } else if (isNearlyFull()) {
            return "Limited spots available";
        } else if (isNearlyEmpty()) {
            return "Plenty of spots available";
        } else {
            return "Spots available";
        }
    }
    
    /**
     * Get formatted occupancy string
     */
    public String getFormattedOccupancy() {
        return String.format("%d/%d spots occupied (%.1f%%)", 
                           occupiedSpots, totalSpots, getOccupancyRate());
    }
    
    /**
     * Get formatted availability string
     */
    public String getFormattedAvailability() {
        return String.format("%d/%d spots available (%.1f%%)", 
                           freeSpots, totalSpots, getAvailabilityRate());
    }
    
    /**
     * Get time formatted for display
     */
    public String getFormattedTime() {
        return asOfTime != null ? asOfTime.format(DateTimeFormatter.ofPattern("HH:mm")) : "Unknown";
    }
    
    @Override
    public String toString() {
        return String.format("ParkingAvailabilitySummary{total=%d, free=%d, occupied=%d, rate=%.1f%%, asOf=%s}",
                           totalSpots, freeSpots, occupiedSpots, getOccupancyRate(), getFormattedTime());
    }
}