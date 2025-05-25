package client;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import common.*;
import parking.model.*;

/**
 * Console interface for the Parking Management System
 */
public class ParkingConsole implements ParkingClientUI, ChatIF {
    
    final public static int DEFAULT_PORT = 5555;
    
    ParkingClient client;
    
    public ParkingConsole(String host, int port) {
        try {
            client = new ParkingClient(host, port, this);
        } catch (IOException exception) {
            System.out.println("Error: Can't setup connection to parking server!");
            System.out.println("Make sure the server is running on " + host + ":" + port);
            System.exit(1);
        }
    }
    
    /**
     * Main interaction loop - waits for console input
     */
    public void accept() {
        try {
            BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
            String message;
            
            displayWelcomeMessage();
            
            while (true) {
                System.out.print("\nParking> ");
                message = fromConsole.readLine();
                
                if (message == null) break;
                
                processCommand(message.trim());
            }
        } catch (Exception ex) {
            System.out.println("Unexpected error while reading from console!");
            ex.printStackTrace();
        }
    }
    
    /**
     * Display welcome message and available commands
     */
    private void displayWelcomeMessage() {
        System.out.println("\n=== Welcome to Parking Management System ===");
        System.out.println("Available commands:");
        System.out.println("park <customer_id>           - Park now with customer ID");
        System.out.println("check                        - Check current availability");
        System.out.println("prebook <customer_id> <date> <time> - Prebook parking");
        System.out.println("status                       - Get parking lot summary");
        System.out.println("help                         - Show this help message");
        System.out.println("quit                         - Exit the application");
        System.out.println("\nDate format: YYYY-MM-DD (e.g., 2024-12-25)");
        System.out.println("Time format: HH:MM (e.g., 14:30)");
        System.out.println("============================================");
    }
    
    /**
     * Process user commands
     */
    private void processCommand(String command) {
        if (command.isEmpty()) {
            return;
        }
        
        String[] parts = command.split("\\s+");
        String cmd = parts[0].toLowerCase();
        
        try {
            switch (cmd) {
                case "park":
                    handleParkCommand(parts);
                    break;
                case "check":
                    handleCheckCommand();
                    break;
                case "prebook":
                    handlePrebookCommand(parts);
                    break;
                case "status":
                    handleStatusCommand();
                    break;
                case "help":
                    displayWelcomeMessage();
                    break;
                case "quit":
                case "exit":
                    handleQuitCommand();
                    break;
                case "ping":
                    client.sendCommand("#PING");
                    break;
                default:
                    System.out.println("Unknown command: " + cmd);
                    System.out.println("Type 'help' for available commands.");
            }
        } catch (Exception e) {
            System.out.println("Error processing command: " + e.getMessage());
        }
    }
    
    private void handleParkCommand(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: park <customer_id>");
            return;
        }
        
        String customerId = parts[1];
        System.out.println("Requesting immediate parking for customer: " + customerId);
        
        ParkingResponse response = client.parkNow(customerId);
    }
    
    private void handleCheckCommand() {
        System.out.println("Checking current parking availability...");
        ParkingResponse response = client.checkAvailability();
    }
    
    private void handlePrebookCommand(String[] parts) {
        if (parts.length < 4) {
            System.out.println("Usage: prebook <customer_id> <date> <time>");
            System.out.println("Example: prebook CUST001 2024-12-25 14:30");
            return;
        }
        
        try {
            String customerId = parts[1];
            LocalDate date = LocalDate.parse(parts[2]);
            LocalTime time = LocalTime.parse(parts[3]);
            
            System.out.printf("Requesting prebooking for customer %s on %s at %s...\n", 
                            customerId, date, time);
            
            ParkingResponse response = client.preBook(customerId, date, time);
            
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date or time format!");
            System.out.println("Date format: YYYY-MM-DD (e.g., 2024-12-25)");
            System.out.println("Time format: HH:MM (e.g., 14:30)");
        }
    }
    
    private void handleStatusCommand() {
        System.out.println("Getting parking lot status...");
        ParkingResponse response = client.getSummary();
    }
    
    private void handleQuitCommand() {
        System.out.println("Disconnecting from parking server...");
        client.quit();
    }
    
    public void display(String message) {
        System.out.println("> " + message);
    }
    
    /**
     * Display parking-specific responses with formatting
     */
    public void displayParkingResponse(ParkingResponse response) {
        System.out.println("\n" + "=".repeat(50));
        
        if (response.isSuccess()) {
            System.out.println("✓ SUCCESS: " + response.getMessage());
            
            if (response.getData() != null) {
                Object data = response.getData();
                
                if (data instanceof ParkingConfirmation) {
                    ParkingConfirmation confirmation = (ParkingConfirmation) data;
                    System.out.println(confirmation.getFormattedDetails());
                    
                } else if (data instanceof CurrentSpotAvailability) {
                    CurrentSpotAvailability spot = (CurrentSpotAvailability) data;
                    System.out.println("Available Spot Details:");
                    System.out.println("Spot Number: #" + spot.getSpotNumber());
                    System.out.println("Duration: " + spot.getFormattedDuration());
                    System.out.println("Available from: " + 
                                     spot.getAvailableFrom().format(DateTimeFormatter.ofPattern("HH:mm")));
                    System.out.println("Free until: " + 
                                     spot.getFreeUntil().format(DateTimeFormatter.ofPattern("HH:mm")));
                    
                } else if (data instanceof ParkingAvailabilitySummary) {
                    ParkingAvailabilitySummary summary = (ParkingAvailabilitySummary) data;
                    System.out.println("Parking Lot Status:");
                    System.out.println("Total spots: " + summary.getTotalSpots());
                    System.out.println("Available: " + summary.getFreeSpots());
                    System.out.println("Occupied: " + summary.getOccupiedSpots());
                    System.out.println("Occupancy rate: " + String.format("%.1f%%", summary.getOccupancyRate()));
                    System.out.println("Status: " + summary.getStatusDescription());
                    
                } else {
                    System.out.println("Data: " + data.toString());
                }
            }
        } else {
            System.out.println("✗ ERROR: " + response.getMessage());
        }
        
        System.out.println("=".repeat(50));
    }
    
    public static void main(String[] args) {
        String host = "localhost";
        int port = DEFAULT_PORT;
        
        try {
            if (args.length > 0) {
                host = args[0];
            }
            if (args.length > 1) {
                port = Integer.parseInt(args[1]);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // Use defaults
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number, using default: " + DEFAULT_PORT);
        }
        
        System.out.println("Connecting to parking server at " + host + ":" + port);
        
        ParkingConsole console = new ParkingConsole(host, port);
        console.accept();
    }
}