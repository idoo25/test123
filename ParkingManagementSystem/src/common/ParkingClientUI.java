package common;

/**
 * Interface for parking client UI implementations
 */
public interface ParkingClientUI {
    /**
     * Display a general message
     */
    void display(String message);
    
    /**
     * Display a parking response with specialized formatting
     */
    void displayParkingResponse(ParkingResponse response);
}