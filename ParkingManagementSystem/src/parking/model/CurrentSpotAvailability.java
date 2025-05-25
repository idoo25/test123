package parking.model;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Serializable version of CurrentSpotAvailability for client-server communication
 */
public class CurrentSpotAvailability implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int spotNumber;
    private double durationHours;
    private LocalTime availableFrom;
    private LocalTime freeUntil;
    
    public CurrentSpotAvailability(int spotNumber, double durationHours, 
                                  LocalTime availableFrom, LocalTime freeUntil) {
        if (spotNumber < 1 || spotNumber > 100) {
            throw new IllegalArgumentException("Spot number must be between 1 and 100");
        }
        if (durationHours < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }
        
        this.spotNumber = spotNumber;
        this.durationHours = durationHours;
        this.availableFrom = availableFrom;
        this.freeUntil = freeUntil;
    }
    
    // Getters
    public int getSpotNumber() { return spotNumber; }
    public double getDurationHours() { return durationHours; }
    public LocalTime getAvailableFrom() { return availableFrom; }
    public LocalTime getFreeUntil() { return freeUntil; }
    
    // Setters
    public void setSpotNumber(int spotNumber) {
        if (spotNumber < 1 || spotNumber > 100) {
            throw new IllegalArgumentException("Spot number must be between 1 and 100");
        }
        this.spotNumber = spotNumber;
    }
    
    public void setDurationHours(double durationHours) {
        if (durationHours < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }
        this.durationHours = durationHours;
    }
    
    public void setAvailableFrom(LocalTime availableFrom) { this.availableFrom = availableFrom; }
    public void setFreeUntil(LocalTime freeUntil) { this.freeUntil = freeUntil; }
    
    /**
     * Get duration in minutes
     */
    public long getDurationMinutes() {
        return (long) (durationHours * 60);
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
    
    /**
     * Check if this spot is available for immediate parking
     */
    public boolean isAvailableNow() {
        return durationHours > 0 && 
               availableFrom != null && 
               freeUntil != null &&
               availableFrom.isBefore(freeUntil);
    }
    
    /**
     * Check if this spot is available for at least the specified duration
     */
    public boolean isAvailableForDuration(double requiredHours) {
        return durationHours >= requiredHours;
    }
    
    @Override
    public String toString() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        return String.format("Spot #%d - Free for %s (until %s)",
                spotNumber, 
                getFormattedDuration(),
                freeUntil != null ? freeUntil.format(timeFormatter) : "??:??");
    }
}