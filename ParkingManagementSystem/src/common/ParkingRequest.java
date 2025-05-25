package common;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Parking request message from client to server
 */
public class ParkingRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private ParkingRequestType requestType;
    private String customerId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    
    public ParkingRequest(ParkingRequestType requestType) {
        this.requestType = requestType;
    }
    
    // Getters and setters
    public ParkingRequestType getRequestType() { return requestType; }
    public void setRequestType(ParkingRequestType requestType) { this.requestType = requestType; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    
    @Override
    public String toString() {
        return String.format("ParkingRequest{type=%s, customerId='%s', date=%s, startTime=%s}", 
                           requestType, customerId, date, startTime);
    }
}
