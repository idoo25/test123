package common;

import java.io.Serializable;

/**
 * Parking response message from server to client
 */
public class ParkingResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    private Object data; // Can be ParkingConfirmation, CurrentSpotAvailability, etc.
    
    public ParkingResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
    
    // Setters
    public void setSuccess(boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
    public void setData(Object data) { this.data = data; }
    
    @Override
    public String toString() {
        return String.format("ParkingResponse{success=%s, message='%s', data=%s}", 
                           success, message, data != null ? data.getClass().getSimpleName() : "null");
    }
}