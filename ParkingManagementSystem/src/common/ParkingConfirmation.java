package common;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Parking confirmation details
 */
public class ParkingConfirmation implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int spotNumber;
    private String customerId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private double durationHours;
    
    public ParkingConfirmation(int spotNumber, String customerId, LocalDate date, 
                             LocalTime startTime, LocalTime endTime, double durationHours) {
        this.spotNumber = spotNumber;
        this.customerId = customerId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationHours = durationHours;
    }
    
    // Getters
    public int getSpotNumber() { return spotNumber; }
    public String getCustomerId() { return customerId; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public double getDurationHours() { return durationHours; }
    
    // Formatted strings for display
    public String getFormattedDuration() {
        if (durationHours >= 1) {
            return String.format("%.1f hours", durationHours);
        } else {
            return String.format("%d minutes", (int)(durationHours * 60));
        }
    }
    
    public String getFormattedDetails() {
        return String.format(
            "Parking Confirmed!\n" +
            "Spot: #%d\n" +
            "Customer: %s\n" +
            "Date: %s\n" +
            "Time: %s - %s\n" +
            "Duration: %s",
            spotNumber, customerId, date, startTime, endTime, getFormattedDuration()
        );
    }
    
    @Override
    public String toString() {
        return String.format("ParkingConfirmation{spot=#%d, customer='%s', date=%s, time=%s-%s, duration=%.1fh}",
                           spotNumber, customerId, date, startTime, endTime, durationHours);
    }
}
