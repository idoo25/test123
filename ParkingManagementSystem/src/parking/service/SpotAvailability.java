package parking.service;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * Helper class for spot availability data
 */
public class SpotAvailability implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int parkingSpotNumber;
    private double maxDurationHours;
    private LocalTime availableUntil;
    
    public SpotAvailability(int spotNumber, double duration, LocalTime until) {
        this.parkingSpotNumber = spotNumber;
        this.maxDurationHours = duration;
        this.availableUntil = until;
    }
    
    public int getParkingSpotNumber() { return parkingSpotNumber; }
    public double getMaxDurationHours() { return maxDurationHours; }
    public LocalTime getAvailableUntil() { return availableUntil; }
}