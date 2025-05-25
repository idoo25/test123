package client;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import ocsf.client.*;
import common.*;
import parking.model.*;

/**
 * Parking Management Client using OCSF framework
 */
public class ParkingClient extends AbstractClient {
    
    private ParkingClientUI clientUI;
    private ParkingResponse lastResponse;
    private boolean waitingForResponse;
    
    public ParkingClient(String host, int port, ParkingClientUI clientUI) throws IOException {
        super(host, port);
        this.clientUI = clientUI;
        this.waitingForResponse = false;
        openConnection();
    }
    
    public void handleMessageFromServer(Object msg) {
        if (msg instanceof ParkingResponse) {
            lastResponse = (ParkingResponse) msg;
            clientUI.displayParkingResponse(lastResponse);
        } else if (msg instanceof String) {
            clientUI.display(msg.toString());
        } else {
            clientUI.display("Received unknown message type: " + msg.getClass().getName());
        }
        
        synchronized (this) {
            waitingForResponse = false;
            notifyAll();
        }
    }
    
    public void handleMessageFromClientUI(String message) {
        try {
            sendToServer(message);
        } catch (IOException e) {
            clientUI.display("Could not send message to server. " + e.getMessage());
            quit();
        }
    }
    
    /**
     * Park now - immediate parking request
     */
    public ParkingResponse parkNow(String customerId) {
        try {
            ParkingRequest request = new ParkingRequest(ParkingRequestType.PARK_NOW);
            request.setCustomerId(customerId);
            
            sendRequestAndWait(request);
            return lastResponse;
            
        } catch (Exception e) {
            clientUI.display("Error parking now: " + e.getMessage());
            return new ParkingResponse(false, "Client error: " + e.getMessage(), null);
        }
    }
    
    /**
     * Check current availability
     */
    public ParkingResponse checkAvailability() {
        try {
            ParkingRequest request = new ParkingRequest(ParkingRequestType.CHECK_AVAILABILITY);
            
            sendRequestAndWait(request);
            return lastResponse;
            
        } catch (Exception e) {
            clientUI.display("Error checking availability: " + e.getMessage());
            return new ParkingResponse(false, "Client error: " + e.getMessage(), null);
        }
    }
    
    /**
     * Make a prebooking
     */
    public ParkingResponse preBook(String customerId, LocalDate date, LocalTime startTime) {
        try {
            ParkingRequest request = new ParkingRequest(ParkingRequestType.PREBOOKING);
            request.setCustomerId(customerId);
            request.setDate(date);
            request.setStartTime(startTime);
            
            sendRequestAndWait(request);
            return lastResponse;
            
        } catch (Exception e) {
            clientUI.display("Error prebooking: " + e.getMessage());
            return new ParkingResponse(false, "Client error: " + e.getMessage(), null);
        }
    }
    
    /**
     * Get parking summary
     */
    public ParkingResponse getSummary() {
        try {
            ParkingRequest request = new ParkingRequest(ParkingRequestType.GET_SUMMARY);
            
            sendRequestAndWait(request);
            return lastResponse;
            
        } catch (Exception e) {
            clientUI.display("Error getting summary: " + e.getMessage());
            return new ParkingResponse(false, "Client error: " + e.getMessage(), null);
        }
    }
    
    /**
     * Send request and wait for response
     */
    private void sendRequestAndWait(ParkingRequest request) throws IOException, InterruptedException {
        synchronized (this) {
            waitingForResponse = true;
            sendToServer(request);
            
            // Wait for response with timeout
            long timeout = 10000; // 10 seconds
            long startTime = System.currentTimeMillis();
            
            while (waitingForResponse && (System.currentTimeMillis() - startTime) < timeout) {
                wait(1000); // Wait 1 second at a time
            }
            
            if (waitingForResponse) {
                throw new IOException("Server response timeout");
            }
        }
    }
    
    /**
     * Send simple string command
     */
    public void sendCommand(String command) {
        try {
            sendToServer(command);
        } catch (IOException e) {
            clientUI.display("Could not send command to server: " + e.getMessage());
        }
    }
    
    protected void connectionEstablished() {
        clientUI.display("Connected to parking server");
    }
    
    protected void connectionClosed() {
        clientUI.display("Connection to server closed");
    }
    
    protected void connectionException(Exception exception) {
        clientUI.display("Connection error: " + exception.getMessage());
    }
    
    public void quit() {
        try {
            closeConnection();
        } catch (IOException e) {
            // Ignore exception on close
        }
        System.exit(0);
    }
    
    public boolean isConnectedToServer() {
        return isConnected();
    }
}