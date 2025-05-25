package client.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import client.ParkingClient;
import common.*;
import java.io.IOException;

/**
 * JavaFX Application for Parking Management System with OCSF integration
 */
public class JavaFXParkingApp extends Application implements ParkingClientUI {
    
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5555;
    
    private ParkingClient client;
    private Stage primaryStage;
    private String serverHost;
    private int serverPort;
    
    private MainMenuController mainMenuController;
    
    @Override
    public void init() throws Exception {
        Parameters params = getParameters();
        java.util.List<String> args = params.getRaw();
        
        serverHost = args.size() > 0 ? args.get(0) : DEFAULT_HOST;
        try {
            serverPort = args.size() > 1 ? Integer.parseInt(args.get(1)) : DEFAULT_PORT;
        } catch (NumberFormatException e) {
            serverPort = DEFAULT_PORT;
        }
        
        try {
            client = new ParkingClient(serverHost, serverPort, this);
            System.out.println("Connected to parking server at " + serverHost + ":" + serverPort);
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
        }
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        
        if (client == null) {
            showConnectionError();
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_menu.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 800);
            
            mainMenuController = loader.getController();
            mainMenuController.setParkingClient(client);
            mainMenuController.setPrimaryStage(primaryStage);
            
            primaryStage.setTitle("Parking Management System - Connected to " + serverHost);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
            
            primaryStage.setOnCloseRequest(e -> {
                if (client != null) {
                    client.quit();
                }
                Platform.exit();
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load application", "Could not load the main interface: " + e.getMessage());
        }
    }
    
    @Override
    public void stop() throws Exception {
        if (client != null) {
            try {
                client.closeConnection();
            } catch (Exception e) {
                // Ignore close errors
            }
        }
        super.stop();
    }
    
    @Override
    public void display(String message) {
        Platform.runLater(() -> {
            System.out.println("Server message: " + message);
        });
    }
    
    @Override
    public void displayParkingResponse(ParkingResponse response) {
        Platform.runLater(() -> {
            System.out.println("Parking response: " + response);
        });
    }
    
    private void showConnectionError() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Connection Error");
            alert.setHeaderText("Cannot connect to parking server");
            alert.setContentText(String.format(
                "Failed to connect to server at %s:%d\n\n" +
                "Please make sure:\n" +
                "• The server is running\n" +
                "• The host and port are correct\n" +
                "• Network connection is available",
                serverHost, serverPort
            ));
            alert.showAndWait();
            Platform.exit();
        });
    }
    
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(title);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    public ParkingClient getParkingClient() {
        return client;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}