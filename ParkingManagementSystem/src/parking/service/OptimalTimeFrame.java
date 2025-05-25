package parking.service;

import parking.model.ParkingOrder;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an optimal time frame with spot assignment
 */
public class OptimalTimeFrame implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private double durationHours;
    private int minFreeSpots;
    private int assignedSpotNumber;
    
    public OptimalTimeFrame(LocalDate date, LocalTime startTime, LocalTime endTime, 
                           double durationHours, int minFreeSpots, int assignedSpotNumber) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationHours = durationHours;
        this.minFreeSpots = minFreeSpots;
        this.assignedSpotNumber = assignedSpotNumber;
    }
    
    // Getters
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public double getDurationHours() { return durationHours; }
    public int getMinFreeSpots() { return minFreeSpots; }
    public int getAssignedSpotNumber() { return assignedSpotNumber; }
    
    /**
     * Check if spot assignment is valid
     */
    public boolean hasValidAssignment() {
        return assignedSpotNumber >= 1 && assignedSpotNumber <= 100;
    }
    
    /**
     * Create a ParkingOrder from this OptimalTimeFrame
     */
    public ParkingOrder toParkingOrder(String subscriberId) {
        if (!isValid() || !hasValidAssignment()) {
            throw new IllegalStateException("Cannot create ParkingOrder from invalid OptimalTimeFrame");
        }
        
        return new ParkingOrder(
            0, // Order ID will be auto-generated
            assignedSpotNumber,
            subscriberId,
            date,
            startTime,
            endTime
        );
    }
    
    /**
     * Check if this time frame is valid
     */
    public boolean isValid() {
        return date != null && 
               startTime != null && 
               endTime != null &&
               durationHours > 0 && 
               minFreeSpots > 0 &&
               startTime.isBefore(endTime);
    }
    
    /**
     * Get assignment description for UI display
     */
    public String getAssignmentDescription() {
        if (!hasValidAssignment()) {
            return "No spot assigned";
        }
        
        return String.format("Spot #%d assigned (%.1f hours)", 
                           assignedSpotNumber, durationHours);
    }
    
    /**
     * Format duration as human readable string
     */
    public String getFormattedDuration() {
        if (durationHours >= 1) {
            return String.format("%.1f hours", durationHours);
        } else {
            return String.format("%d minutes", (int)(durationHours * 60));
        }
    }
    
    @Override
    public String toString() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        if (!hasValidAssignment()) {
            return String.format("%s: %s-%s (%s, %d free spots)",
                    date, startTime.format(timeFormatter), endTime.format(timeFormatter),
                    getFormattedDuration(), minFreeSpots);
        }
        
        return String.format("%s → Gets %s (Spot #%d assigned)",
                startTime.format(timeFormatter),
                getFormattedDuration(),
                assignedSpotNumber);
    }
}