package parking.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Serializable version of ParkingOrder
 */
public class ParkingOrder implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int orderId;
    private int parkingSpotNumber;
    private String subscriberId;
    private LocalDate dateOfParking;
    private LocalDate datePlacingOrder;
    private LocalTime timeOfCarDeposit;
    private LocalTime timeOfRetrievalTime;
    
    public ParkingOrder() {
        this.datePlacingOrder = LocalDate.now();
    }
    
    public ParkingOrder(int orderId, int parkingSpotNumber, String subscriberId,
                       LocalDate dateOfParking, LocalTime timeOfCarDeposit, 
                       LocalTime timeOfRetrievalTime) {
        this.orderId = orderId;
        this.parkingSpotNumber = parkingSpotNumber;
        this.subscriberId = subscriberId;
        this.dateOfParking = dateOfParking;
        this.datePlacingOrder = LocalDate.now();
        this.timeOfCarDeposit = timeOfCarDeposit;
        this.timeOfRetrievalTime = timeOfRetrievalTime;
    }
    
    public ParkingOrder(int parkingSpotNumber, String subscriberId,
                       LocalTime timeOfCarDeposit, LocalTime timeOfRetrievalTime) {
        this(0, parkingSpotNumber, subscriberId, LocalDate.now(), 
             timeOfCarDeposit, timeOfRetrievalTime);
    }
    
    // Getters and Setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    
    public int getParkingSpotNumber() { return parkingSpotNumber; }
    public void setParkingSpotNumber(int parkingSpotNumber) {
        if (parkingSpotNumber < 1 || parkingSpotNumber > 100) {
            throw new IllegalArgumentException("Parking spot number must be between 1 and 100");
        }
        this.parkingSpotNumber = parkingSpotNumber;
    }
    
    public String getSubscriberId() { return subscriberId; }
    public void setSubscriberId(String subscriberId) {
        if (subscriberId == null || subscriberId.trim().isEmpty()) {
            throw new IllegalArgumentException("Subscriber ID cannot be null or empty");
        }
        this.subscriberId = subscriberId;
    }
    
    public LocalDate getDateOfParking() { return dateOfParking; }
    public void setDateOfParking(LocalDate dateOfParking) {
        if (dateOfParking == null) {
            throw new IllegalArgumentException("Date of parking cannot be null");
        }
        this.dateOfParking = dateOfParking;
    }
    
    public LocalDate getDatePlacingOrder() { return datePlacingOrder; }
    public void setDatePlacingOrder(LocalDate datePlacingOrder) { this.datePlacingOrder = datePlacingOrder; }
    
    public LocalTime getTimeOfCarDeposit() { return timeOfCarDeposit; }
    public void setTimeOfCarDeposit(LocalTime timeOfCarDeposit) {
        if (timeOfCarDeposit == null) {
            throw new IllegalArgumentException("Time of car deposit cannot be null");
        }
        this.timeOfCarDeposit = timeOfCarDeposit;
    }
    
    public LocalTime getTimeOfRetrievalTime() { return timeOfRetrievalTime; }
    public void setTimeOfRetrievalTime(LocalTime timeOfRetrievalTime) { this.timeOfRetrievalTime = timeOfRetrievalTime; }
    
    /**
     * Calculate parking duration in hours
     */
    public double getDurationHours() {
        if (timeOfCarDeposit == null || timeOfRetrievalTime == null) {
            return 0.0;
        }
        
        long minutes = java.time.Duration.between(timeOfCarDeposit, timeOfRetrievalTime).toMinutes();
        return minutes / 60.0;
    }
    
    /**
     * Check if this is a valid parking order
     */
    public boolean isValid() {
        return parkingSpotNumber >= 1 && parkingSpotNumber <= 100 &&
               subscriberId != null && !subscriberId.trim().isEmpty() &&
               dateOfParking != null &&
               timeOfCarDeposit != null;
    }
    
    @Override
    public String toString() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        return String.format("ParkingOrder{orderId=%d, spot=#%d, customer='%s', date=%s, time=%s-%s, duration=%.1fh}",
                orderId, 
                parkingSpotNumber, 
                subscriberId,
                dateOfParking != null ? dateOfParking.format(dateFormatter) : "null",
                timeOfCarDeposit != null ? timeOfCarDeposit.format(timeFormatter) : "null",
                timeOfRetrievalTime != null ? timeOfRetrievalTime.format(timeFormatter) : "null",
                getDurationHours());
    }
}