package client.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;  // ADDED: Missing VBox import
import javafx.scene.layout.HBox;  // ADDED: Missing HBox import
import javafx.scene.layout.GridPane;  // ADDED: Missing GridPane import
import javafx.scene.paint.Color;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.application.Platform;

import client.ParkingClient;
import common.*;
import parking.model.*;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Park Now immediate parking interface - FIXED VERSION
 */
public class ParkNowController implements Initializable {
    
    @FXML private Label currentTimeLabel;
    @FXML private Label availabilityStatusLabel;
    @FXML private ProgressBar occupancyBar;
    @FXML private Label occupancyLabel;
    
    @FXML private VBox availableSpotArea;  // Now VBox is properly imported
    @FXML private Label bestSpotLabel;
    @FXML private Label durationLabel;
    @FXML private Label validUntilLabel;
    
    @FXML private TextField customerIdField;
    @FXML private Button checkAvailabilityButton;
    @FXML private Button parkNowButton;
    @FXML private Button refreshButton;
    
    private ParkingClient parkingClient;
    private CurrentSpotAvailability currentBestSpot;
    private Timeline refreshTimeline;
    
    public void setParkingClient(ParkingClient client) {
        this.parkingClient = client;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        availableSpotArea.setVisible(false);
        parkNowButton.setDisable(true);
        
        startAutoRefresh();
        refreshAvailability();
    }
    
    private void startAutoRefresh() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> refreshAvailability()));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }
    
    @FXML
    private void refreshAvailability() {
        updateCurrentTime();
        updateAvailabilitySummary();
    }
    
    @FXML
    private void checkAvailability() {
        if (parkingClient == null) {
            showError("No connection to parking server");
            return;
        }
        
        checkAvailabilityButton.setDisable(true);
        checkAvailabilityButton.setText("Checking...");
        
        new Thread(() -> {
            try {
                ParkingResponse response = parkingClient.checkAvailability();
                
                Platform.runLater(() -> {
                    handleAvailabilityResponse(response);
                    
                    checkAvailabilityButton.setDisable(false);
                    checkAvailabilityButton.setText("Check What's Available Now");
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Failed to check availability: " + e.getMessage());
                    checkAvailabilityButton.setDisable(false);
                    checkAvailabilityButton.setText("Check What's Available Now");
                });
            }
        }).start();
    }
    
    private void handleAvailabilityResponse(ParkingResponse response) {
        if (response.isSuccess() && response.getData() instanceof CurrentSpotAvailability) {
            currentBestSpot = (CurrentSpotAvailability) response.getData();
            showAvailableSpot(currentBestSpot);
            availabilityStatusLabel.setText("✓ Parking Available Now!");
            availabilityStatusLabel.setTextFill(Color.GREEN);
        } else {
            currentBestSpot = null;
            hideAvailableSpot();
            availabilityStatusLabel.setText("✗ " + response.getMessage());
            availabilityStatusLabel.setTextFill(Color.RED);
        }
    }
    
    private void showAvailableSpot(CurrentSpotAvailability spot) {
        if (spot == null) return;
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        bestSpotLabel.setText(String.format("Spot #%d", spot.getSpotNumber()));
        durationLabel.setText(String.format("Available for %s", spot.getFormattedDuration()));
        validUntilLabel.setText(String.format("Valid until %s", spot.getFreeUntil().format(timeFormatter)));
        
        availableSpotArea.setVisible(true);
        parkNowButton.setDisable(false);
    }
    
    private void hideAvailableSpot() {
        availableSpotArea.setVisible(false);
        parkNowButton.setDisable(true);
    }
    
    @FXML
    private void parkNow() {
        if (parkingClient == null) {
            showError("No connection to parking server");
            return;
        }
        
        if (currentBestSpot == null) {
            showError("No spot selected. Please check availability first.");
            return;
        }
        
        String customerId = customerIdField.getText().trim();
        if (customerId.isEmpty()) {
            showError("Please enter your Customer ID.");
            customerIdField.requestFocus();
            return;
        }
        
        if (showConfirmationDialog(customerId)) {
            processParkNow(customerId);
        }
    }
    
    private boolean showConfirmationDialog(String customerId) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Immediate Parking");
        confirmAlert.setHeaderText("Park Your Car Now?");
        confirmAlert.setContentText(String.format(
            "Spot: #%d\n" +
            "Duration: %s\n" +
            "Valid until: %s\n" +
            "Customer ID: %s\n\n" +
            "Confirm parking now?",
            currentBestSpot.getSpotNumber(),
            currentBestSpot.getFormattedDuration(),
            currentBestSpot.getFreeUntil().format(DateTimeFormatter.ofPattern("HH:mm")),
            customerId
        ));
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    private void processParkNow(String customerId) {
        parkNowButton.setDisable(true);
        parkNowButton.setText("Processing...");
        
        new Thread(() -> {
            try {
                ParkingResponse response = parkingClient.parkNow(customerId);
                
                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        showSuccessDialog(response);
                        
                        customerIdField.clear();
                        currentBestSpot = null;
                        hideAvailableSpot();
                        refreshAvailability();
                        
                    } else {
                        showError("Parking failed: " + response.getMessage());
                        checkAvailability();
                    }
                    
                    parkNowButton.setText("🚗 Park My Car Now!");
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Parking failed: " + e.getMessage());
                    parkNowButton.setText("🚗 Park My Car Now!");
                });
            }
        }).start();
    }
    
    private void showSuccessDialog(ParkingResponse response) {
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Parking Confirmed");
        successAlert.setHeaderText("Your car is now parked!");
        
        if (response.getData() instanceof ParkingConfirmation) {
            ParkingConfirmation confirmation = (ParkingConfirmation) response.getData();
            successAlert.setContentText(confirmation.getFormattedDetails());
        } else {
            successAlert.setContentText(response.getMessage());
        }
        
        successAlert.showAndWait();
    }
    
    private void updateCurrentTime() {
        LocalTime currentTime = LocalTime.now();
        currentTimeLabel.setText("Current time: " + 
                                currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
    
    private void updateAvailabilitySummary() {
        if (parkingClient == null) return;
        
        new Thread(() -> {
            try {
                ParkingResponse response = parkingClient.getSummary();
                
                if (response.isSuccess() && response.getData() instanceof ParkingAvailabilitySummary) {
                    ParkingAvailabilitySummary summary = (ParkingAvailabilitySummary) response.getData();
                    
                    Platform.runLater(() -> {
                        occupancyLabel.setText(summary.getFormattedOccupancy());
                        occupancyBar.setProgress(summary.getOccupancyRate() / 100.0);
                        updateAvailabilityStatus(summary);
                    });
                }
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    occupancyLabel.setText("Unable to load occupancy data");
                });
            }
        }).start();
    }
    
    private void updateAvailabilityStatus(ParkingAvailabilitySummary summary) {
        if (summary.isFull()) {
            availabilityStatusLabel.setText("✗ Parking Lot Full");
            availabilityStatusLabel.setTextFill(Color.RED);
            checkAvailabilityButton.setDisable(true);
        } else if (summary.isNearlyFull()) {
            availabilityStatusLabel.setText("⚠ Limited Spots Available");
            availabilityStatusLabel.setTextFill(Color.ORANGE);
            checkAvailabilityButton.setDisable(false);
        } else {
            availabilityStatusLabel.setText("✓ Spots Available");
            availabilityStatusLabel.setTextFill(Color.GREEN);
            checkAvailabilityButton.setDisable(false);
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void shutdown() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
    }
}