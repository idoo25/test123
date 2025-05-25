package parking.service;

import parking.model.ParkingOrder;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Parking assignment result
 */
public class ParkingAssignment implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int assignedSpotNumber;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private double durationHours;
    
    public ParkingAssignment(int spotNumber, LocalDate date, LocalTime startTime, 
                           LocalTime endTime, double duration) {
        this.assignedSpotNumber = spotNumber;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationHours = duration;
    }
    
    public int getAssignedSpotNumber() { return assignedSpotNumber; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public double getDurationHours() { return durationHours; }
    
    public ParkingOrder toParkingOrder(String subscriberId) {
        return new ParkingOrder(
            0, assignedSpotNumber, subscriberId, date, startTime, endTime
        );
    }
}