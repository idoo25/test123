package server;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import ocsf.server.*;
import common.*;
import parking.service.*;
import parking.model.*;
import parking.util.*;

/**
 * Parking Management Server using OCSF framework
 */
public class ParkingServer extends AbstractServer {
    
    final public static int DEFAULT_PORT = 5555;
    
    private Connection dbConnection;
    private ParkNowService parkNowService;
    private ParkingSpotAssigner spotAssigner;
    
    public ParkingServer(int port) {
        super(port);
        initializeDatabase();
    }
    
    /**
     * Initialize database connection and services
     */
    private void initializeDatabase() {
        try {
            dbConnection = MySQLConnectionAdapter.getConnection();
            parkNowService = new ParkNowService(dbConnection);
            spotAssigner = new ParkingSpotAssigner(dbConnection);
            
        } catch (Exception ex) {
            System.err.println("Database initialization failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * Handle messages from clients
     */
    public void handleMessageFromClient(Object msg, ConnectionToClient client) {
        System.out.println("Message received: " + msg + " from " + client);
        
        if (msg instanceof ParkingRequest) {
            handleParkingRequest((ParkingRequest) msg, client);
        } else if (msg instanceof String) {
            handleStringCommand((String) msg, client);
        } else {
            try {
                client.sendToClient(new ParkingResponse(false, "Unknown command type", null));
            } catch (IOException e) {
                System.err.println("Error sending response to client: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handle parking-specific requests
     */
    private void handleParkingRequest(ParkingRequest request, ConnectionToClient client) {
        try {
            ParkingResponse response = null;
            
            switch (request.getRequestType()) {
                case PARK_NOW:
                    response = handleParkNowRequest(request);
                    break;
                case CHECK_AVAILABILITY:
                    response = handleAvailabilityCheck(request);
                    break;
                case PREBOOKING:
                    response = handlePrebookingRequest(request);
                    break;
                case GET_SUMMARY:
                    response = handleSummaryRequest();
                    break;
                default:
                    response = new ParkingResponse(false, "Unknown request type", null);
            }
            
            client.sendToClient(response);
            
        } catch (IOException e) {
            System.err.println("Error handling parking request: " + e.getMessage());
        }
    }
    
    /**
     * Handle immediate parking requests
     */
    private ParkingResponse handleParkNowRequest(ParkingRequest request) {
        try {
            String customerId = request.getCustomerId();
            if (customerId == null || customerId.trim().isEmpty()) {
                return new ParkingResponse(false, "Customer ID is required", null);
            }
            
            // First check availability
            ParkNowResult availabilityResult = parkNowService.checkAvailableNow();
            if (!availabilityResult.isAvailable()) {
                return new ParkingResponse(false, availabilityResult.getMessage(), null);
            }
            
            // Park the car
            CurrentSpotAvailability bestSpot = availabilityResult.getAssignedSpot();
            boolean success = parkNowService.parkNow(bestSpot, customerId);
            
            if (success) {
                ParkingConfirmation confirmation = new ParkingConfirmation(
                    bestSpot.getSpotNumber(),
                    customerId,
                    LocalDate.now(),
                    bestSpot.getAvailableFrom(),
                    bestSpot.getFreeUntil(),
                    bestSpot.getDurationHours()
                );
                return new ParkingResponse(true, "Parking confirmed", confirmation);
            } else {
                return new ParkingResponse(false, "Failed to park - spot may no longer be available", null);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing park now request: " + e.getMessage());
            return new ParkingResponse(false, "Server error: " + e.getMessage(), null);
        }
    }
    
    /**
     * Handle availability check requests
     */
    private ParkingResponse handleAvailabilityCheck(ParkingRequest request) {
        try {
            ParkNowResult result = parkNowService.checkAvailableNow();
            
            if (result.isAvailable()) {
                CurrentSpotAvailability spot = result.getAssignedSpot();
                return new ParkingResponse(true, 
                    String.format("Best spot: #%d available for %s", 
                                 spot.getSpotNumber(), spot.getFormattedDuration()), 
                    spot);
            } else {
                return new ParkingResponse(false, result.getMessage(), null);
            }
            
        } catch (Exception e) {
            System.err.println("Error checking availability: " + e.getMessage());
            return new ParkingResponse(false, "Server error: " + e.getMessage(), null);
        }
    }
    
    /**
     * Handle prebooking requests
     */
    private ParkingResponse handlePrebookingRequest(ParkingRequest request) {
        try {
            LocalDate date = request.getDate();
            LocalTime startTime = request.getStartTime();
            String customerId = request.getCustomerId();
            
            if (date == null || startTime == null || customerId == null) {
                return new ParkingResponse(false, "Date, time, and customer ID are required for prebooking", null);
            }
            
            // Find optimal spot assignment
            ParkingAssignment assignment = spotAssigner.assignOptimalSpot(date, startTime);
            
            if (assignment == null) {
                return new ParkingResponse(false, "No spots available for the requested time", null);
            }
            
            // Create and save the parking order
            ParkingOrder order = assignment.toParkingOrder(customerId);
            boolean success = saveParkingOrder(order);
            
            if (success) {
                ParkingConfirmation confirmation = new ParkingConfirmation(
                    assignment.getAssignedSpotNumber(),
                    customerId,
                    assignment.getDate(),
                    assignment.getStartTime(),
                    assignment.getEndTime(),
                    assignment.getDurationHours()
                );
                return new ParkingResponse(true, "Prebooking confirmed", confirmation);
            } else {
                return new ParkingResponse(false, "Failed to save prebooking", null);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing prebooking request: " + e.getMessage());
            return new ParkingResponse(false, "Server error: " + e.getMessage(), null);
        }
    }
    
    /**
     * Handle summary requests
     */
    private ParkingResponse handleSummaryRequest() {
        try {
            ParkingAvailabilitySummary summary = parkNowService.getCurrentAvailabilitySummary();
            return new ParkingResponse(true, "Summary retrieved", summary);
            
        } catch (Exception e) {
            System.err.println("Error getting summary: " + e.getMessage());
            return new ParkingResponse(false, "Server error: " + e.getMessage(), null);
        }
    }
    
    /**
     * Handle string commands (legacy support)
     */
    private void handleStringCommand(String command, ConnectionToClient client) {
        try {
            if (command.startsWith("#STATUS")) {
                ParkingAvailabilitySummary summary = parkNowService.getCurrentAvailabilitySummary();
                client.sendToClient("STATUS: " + summary.getFormattedOccupancy());
                
            } else if (command.startsWith("#PING")) {
                client.sendToClient("PONG: Server is alive");
                
            } else {
                client.sendToClient("Unknown command: " + command);
            }
            
        } catch (IOException e) {
            System.err.println("Error handling string command: " + e.getMessage());
        }
    }
    
    /**
     * Save parking order to database
     */
    private boolean saveParkingOrder(ParkingOrder order) {
        try {
            String insertOrder = """
                INSERT INTO parking_orders 
                (parking_spot_number, subscriber_id, date_of_parking, date_placing_order, 
                 time_of_car_deposit, time_of_retrieval_time)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
            
            try (PreparedStatement stmt = dbConnection.prepareStatement(insertOrder)) {
                stmt.setInt(1, order.getParkingSpotNumber());
                stmt.setString(2, order.getSubscriberId());
                stmt.setDate(3, Date.valueOf(order.getDateOfParking()));
                stmt.setDate(4, Date.valueOf(LocalDate.now()));
                stmt.setTime(5, Time.valueOf(order.getTimeOfCarDeposit()));
                stmt.setTime(6, Time.valueOf(order.getTimeOfRetrievalTime()));
                
                int result = stmt.executeUpdate();
                return result > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error saving parking order: " + e.getMessage());
            return false;
        }
    }
    
    protected void serverStarted() {
        System.out.println("Parking Server listening for connections on port " + getPort());
        System.out.println("Database connection: " + (dbConnection != null ? "Connected" : "Failed"));
    }
    
    protected void serverStopped() {
        System.out.println("Parking Server has stopped listening for connections.");
    }
    
    protected void clientConnected(ConnectionToClient client) {
        System.out.println("New client connected: " + client);
        super.clientConnected(client);
    }
    
    protected void clientDisconnected(ConnectionToClient client) {
        System.out.println("Client disconnected: " + client);
        super.clientDisconnected(client);
    }
    
    protected void serverClosed() {
        try {
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
        super.serverClosed();
    }
    
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        
        try {
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }
        } catch (Throwable t) {
            System.out.println("Using default port: " + DEFAULT_PORT);
        }
        
        ParkingServer server = new ParkingServer(port);
        
        try {
            server.listen();
        } catch (Exception ex) {
            System.out.println("ERROR - Could not listen for clients!");
            ex.printStackTrace();
        }
    }
}