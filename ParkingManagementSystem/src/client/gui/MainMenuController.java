package client.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import client.ParkingClient;

/**
 * Controller for the main menu navigation - adapted for OCSF client-server
 */
public class MainMenuController {
    
    @FXML private Button parkNowButton;
    @FXML private Button preBookButton;
    @FXML private Button exitButton;
    
    private ParkingClient parkingClient;
    private Stage primaryStage;
    
    public void setParkingClient(ParkingClient client) {
        this.parkingClient = client;
    }
    
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
    
    @FXML
    private void openParkNow() {
        if (parkingClient == null) {
            showError("No Connection", "Not connected to parking server");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/park_now_view.fxml"));
            Scene scene = new Scene(loader.load(), 800, 700);
            
            ParkNowController controller = loader.getController();
            controller.setParkingClient(parkingClient);
            
            Stage stage = new Stage();
            stage.setTitle("Park Now - Immediate Parking");
            stage.setScene(scene);
            stage.show();
            
            primaryStage.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open Park Now interface", e.getMessage());
        }
    }
    
    @FXML
    private void openPreBooking() {
        if (parkingClient == null) {
            showError("No Connection", "Not connected to parking server");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/calendar_view.fxml"));
            Scene scene = new Scene(loader.load(), 900, 700);
            
            CalendarController controller = loader.getController();
            controller.setParkingClient(parkingClient);
            
            Stage stage = new Stage();
            stage.setTitle("Pre-book Parking - Calendar View");
            stage.setScene(scene);
            stage.show();
            
            primaryStage.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open pre-booking interface", e.getMessage());
        }
    }
    
    @FXML
    private void exitApplication() {
        if (parkingClient != null) {
            parkingClient.quit();
        }
        primaryStage.close();
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}