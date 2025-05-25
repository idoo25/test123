package client.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import client.ParkingClient;
import common.ParkingResponse;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CalendarController implements Initializable {
    
    @FXML private Button prevMonthButton;
    @FXML private Button nextMonthButton;
    @FXML private Label monthYearLabel;
    @FXML private GridPane calendarGrid;
    @FXML private VBox timeSlotArea;
    @FXML private Label selectedDateLabel;
    @FXML private ComboBox<String> timeSlotComboBox;
    @FXML private Label assignedSpotLabel;
    @FXML private Label durationLabel;
    @FXML private TextField customerIdField;
    @FXML private Button confirmBookingButton;
    
    private ParkingClient parkingClient;
    private LocalDate currentDate = LocalDate.now();
    private LocalDate selectedDate;
    private List<Button> dateButtons = new ArrayList<>();
    private YearMonth currentYearMonth;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentYearMonth = YearMonth.from(currentDate);
        updateCalendar();
        
        timeSlotComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                updateAssignedSpot(newValue);
                confirmBookingButton.setDisable(false);
            }
        });
    }
    
    public void setParkingClient(ParkingClient client) {
        this.parkingClient = client;
    }
    
    @FXML
    private void previousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendar();
    }
    
    @FXML
    private void nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendar();
    }
    
    private void updateCalendar() {
        // Clear previous content
        calendarGrid.getChildren().clear();
        dateButtons.clear();
        
        // Update month/year label
        monthYearLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        
        // Add day of week labels (Mon, Tue, etc.)
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(dayNames[i]);
            dayLabel.setStyle("-fx-font-weight: bold;");
            calendarGrid.add(dayLabel, i, 0);
        }
        
        // Get date info
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;  // 0 = Sunday, 6 = Saturday
        int daysInMonth = currentYearMonth.lengthOfMonth();
        
        // Today and 7 days ahead for booking window
        LocalDate today = LocalDate.now();
        LocalDate maxBookingDate = today.plusDays(7);
        
        // Add date buttons to calendar grid
        for (int i = 0; i < daysInMonth; i++) {
            LocalDate date = firstOfMonth.plusDays(i);
            Button dateButton = new Button(String.valueOf(date.getDayOfMonth()));
            dateButton.setPrefWidth(40);
            dateButton.setPrefHeight(40);
            
            // Style based on availability
            if (date.isBefore(today)) {
                // Past dates
                dateButton.setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #d32f2f;");
                dateButton.setDisable(true);
            } else if (date.isAfter(maxBookingDate)) {
                // Too far ahead
                dateButton.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #9e9e9e;");
                dateButton.setDisable(true);
            } else {
                // Available for booking
                dateButton.setStyle("-fx-background-color: #c8e6c9; -fx-text-fill: #2e7d32;");
                
                // Add click handler
                final LocalDate clickedDate = date;
                dateButton.setOnAction(event -> selectDate(clickedDate));
            }
            
            // Calculate grid position
            int row = ((i + dayOfWeek) / 7) + 1;  // +1 for header row
            int column = (i + dayOfWeek) % 7;
            calendarGrid.add(dateButton, column, row);
            dateButtons.add(dateButton);
        }
    }
    
    private void selectDate(LocalDate date) {
        selectedDate = date;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy");
        selectedDateLabel.setText("Selected: " + date.format(formatter));
        
        // Reset previous selection styling
        for (Button btn : dateButtons) {
            if (!btn.isDisabled()) {
                btn.setStyle("-fx-background-color: #c8e6c9; -fx-text-fill: #2e7d32;");
            }
        }
        
        // Highlight selected date
        Button selectedButton = dateButtons.get(date.getDayOfMonth() - 1);
        selectedButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        
        // Load time slots
        loadAvailableTimeSlots(date);
        
        // Show time slot selection area
        timeSlotArea.setVisible(true);
    }
    
    private void loadAvailableTimeSlots(LocalDate date) {
        timeSlotComboBox.getItems().clear();
        confirmBookingButton.setDisable(true);
        
        // Sample time slots (in real app, would come from server)
        List<String> timeSlots = new ArrayList<>();
        LocalTime startTime = LocalTime.of(8, 0);
        
        for (int i = 0; i < 20; i++) {
            LocalTime time = startTime.plusMinutes(i * 30);
            if (time.isBefore(LocalTime.of(20, 0))) {  // End at 8 PM
                double duration = 3.5 - (i * 0.25);  // Decreasing duration
                if (duration >= 0.5) {  // At least 30 min
                    timeSlots.add(String.format("%s - %s hours", 
                            time.format(DateTimeFormatter.ofPattern("HH:mm")),
                            duration));
                }
            }
        }
        
        timeSlotComboBox.getItems().addAll(timeSlots);
    }
    
    private void updateAssignedSpot(String timeSlotText) {
        // Extract time from selected slot
        String timeStr = timeSlotText.split(" - ")[0];
        String durationStr = timeSlotText.split(" - ")[1];
        
        // In a real app, this would get the actual assigned spot from the server
        int spotNumber = 10 + (int)(Math.random() * 90);  // Random spot between 10-99
        
        assignedSpotLabel.setText("Assigned Spot: #" + spotNumber);
        durationLabel.setText("Duration: " + durationStr);
    }
    
    @FXML
    private void confirmBooking() {
        if (parkingClient == null) {
            showError("Not connected to parking server");
            return;
        }
        
        String customerId = customerIdField.getText().trim();
        if (customerId.isEmpty()) {
            showError("Please enter your Customer ID");
            customerIdField.requestFocus();
            return;
        }
        
        if (selectedDate == null) {
            showError("No date selected");
            return;
        }
        
        String selectedTimeSlot = timeSlotComboBox.getValue();
        if (selectedTimeSlot == null) {
            showError("Please select a time slot");
            return;
        }
        
        // Extract time from selected slot
        String timeStr = selectedTimeSlot.split(" - ")[0];
        LocalTime startTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        
        // Process booking with server
        try {
            ParkingResponse response = parkingClient.preBook(customerId, selectedDate, startTime);
            
            if (response.isSuccess()) {
                showSuccessDialog(response.getMessage());
                timeSlotArea.setVisible(false);
            } else {
                showError(response.getMessage());
            }
        } catch (Exception e) {
            showError("Failed to book: " + e.getMessage());
        }
    }
    
    private void showSuccessDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Confirmed");
        alert.setHeaderText("Your parking has been pre-booked!");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    private void openParkNow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/park_now_view.fxml"));
            Scene scene = new Scene(loader.load(), 800, 700);
            
            ParkNowController controller = loader.getController();
            controller.setParkingClient(parkingClient);
            
            Stage currentStage = (Stage) prevMonthButton.getScene().getWindow();
            Stage stage = new Stage();
            stage.setTitle("Park Now - Immediate Parking");
            stage.setScene(scene);
            stage.show();
            
            currentStage.close();
            
        } catch (IOException e) {
            showError("Failed to open Park Now interface: " + e.getMessage());
        }
    }
    
    @FXML
    private void openPreBooking() {
        // Already in pre-booking screen
    }
    
    @FXML
    private void exitApplication() {
        if (parkingClient != null) {
            parkingClient.quit();
        }
        Stage stage = (Stage) prevMonthButton.getScene().getWindow();
        stage.close();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
