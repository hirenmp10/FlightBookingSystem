package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Booking;
import dao.BookingDAO;
import utils.BookingContext;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import utils.SceneSwitcher;
import service.NotificationService;


import java.util.HashSet;
import java.util.Set;

public class BookingController {

    @FXML private TextField userIdField;
    @FXML private TextField flightNumberField;
    @FXML private TextField nameField;
    @FXML private TextField dobField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField addressField;
    @FXML private ComboBox<String> seatsDropdown;
    @FXML private VBox passengerDetailsContainer;
    @FXML private Button confirmButton;
    @FXML private VBox seatLayoutContainer;
    @FXML private TextField seatNumbersField;
    @FXML private Button payButton;
    @FXML private Label paymentLabel;

    private int userId;
    private String flightNumber;
    private Set<Button> selectedSeats = new HashSet<>();
    private int totalSeats;
    private int seatsToSelect = 0;

    @FXML
    public void initialize() {
        flightNumber = BookingContext.getSelectedFlightNumber();
        flightNumberField.setText(flightNumber);
        flightNumberField.setEditable(false);

        userId = BookingContext.getLoggedInUserId();
        userIdField.setText(String.valueOf(userId));
        userIdField.setEditable(false);

        seatsDropdown.getItems().addAll("1", "2", "3", "4");
        seatsDropdown.setValue("Select");

        // Fetch totalSeats from the database
        totalSeats = BookingDAO.getTotalSeats(flightNumber);

        updateSeatLayout();
    }

    @FXML
    public void handleSeatSelection(ActionEvent event) {
        seatsToSelect = Integer.parseInt(seatsDropdown.getValue());
        selectedSeats.clear();
        seatLayoutContainer.getChildren().clear();
        passengerDetailsContainer.getChildren().clear();
        updateSeatLayout();
        updatePassengerDetailsForm();
    }

    private void updateSeatLayout() {
        int rows = 5;  // Number of rows in the seating layout
        int cols = (totalSeats + rows - 1) / rows;  // Calculate number of columns based on total seats

        seatLayoutContainer.getChildren().clear();  // Clear the current layout

        for (int i = 0; i < rows; i++) {
            HBox row = new HBox(5);  // Create a horizontal box for each row
            for (int j = i; j < totalSeats; j += rows) {
                int seatNum = j + 1;
                if (seatNum > totalSeats) break;  // If the seat number exceeds totalSeats, stop

                Button seatButton = new Button(String.valueOf(seatNum));
                
                // Set consistent size for both booked and available seats
                seatButton.setPrefSize(30, 30);  // Set preferred size for both types of buttons
                seatButton.setStyle("-fx-font-size:10px;");  // Use larger font size for better visibility
                
                if (BookingDAO.isSeatBooked(flightNumber, seatNum)) {  // Check if the seat is booked
                    seatButton.setStyle("-fx-background-color: red;-fx-text-fill: white; -fx-font-size: 10px;");
                    seatButton.setDisable(true);  // Disable booked seats
                } else {
                    seatButton.setStyle("-fx-background-color: green; -fx-font-size: 10px;");
                    seatButton.setOnAction(e -> handleSeatSelectionAction(seatButton, seatNum));  // Action for available seats
                }

                row.getChildren().add(seatButton);  // Add the seat button to the row
            }
            seatLayoutContainer.getChildren().add(row);  // Add the row to the seat layout container
        }
    }

    private void updatePassengerDetailsForm() {
        passengerDetailsContainer.getChildren().clear();

        if (seatsToSelect <= 1) {
            // If only 1 seat, show a friendly placeholder
            javafx.scene.control.Label hint = new javafx.scene.control.Label("Only 1 seat selected — no extra passenger details needed.");
            hint.setStyle("-fx-text-fill: #888; -fx-font-size: 13px; -fx-font-style: italic;");
            passengerDetailsContainer.getChildren().add(hint);
            return;
        }

        for (int i = 0; i < seatsToSelect - 1; i++) {
            // === Passenger Card Container ===
            VBox card = new VBox(10);
            card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #d0e4ff;" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 6, 0, 0, 2);"
            );
            card.setPadding(new javafx.geometry.Insets(15, 20, 15, 20));

            // Card header label
            javafx.scene.control.Label header = new javafx.scene.control.Label("🧳 Passenger " + (i + 2) + " Details");
            header.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4C8BF5;");

            // Separator
            javafx.scene.control.Separator sep = new javafx.scene.control.Separator();

            // Name Row
            javafx.scene.control.Label nameLabel = new javafx.scene.control.Label("Full Name:");
            nameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #444; -fx-font-weight: bold;");
            TextField passengerName = new TextField();
            passengerName.setPromptText("Enter full name of Passenger " + (i + 2));
            passengerName.setStyle(
                "-fx-background-radius: 8;" +
                "-fx-border-color: #ccc;" +
                "-fx-border-radius: 8;" +
                "-fx-font-size: 13px;" +
                "-fx-pref-height: 35px;"
            );

            // Date of Birth Row
            javafx.scene.control.Label dobLabel = new javafx.scene.control.Label("Date of Birth (YYYY-MM-DD):");
            dobLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #444; -fx-font-weight: bold;");
            TextField passengerDob = new TextField();
            passengerDob.setPromptText("e.g. 1995-06-15");
            passengerDob.setStyle(
                "-fx-background-radius: 8;" +
                "-fx-border-color: #ccc;" +
                "-fx-border-radius: 8;" +
                "-fx-font-size: 13px;" +
                "-fx-pref-height: 35px;"
            );

            card.getChildren().addAll(header, sep, nameLabel, passengerName, dobLabel, passengerDob);
            passengerDetailsContainer.getChildren().add(card);
        }
    }


    private void handleSeatSelectionAction(Button seatButton, int seatNum) {
        if (seatButton.getStyle().contains("red")) {
            return; // Booked seat, can't select
        }

        if (seatButton.getStyle().contains("green")) {
            if (selectedSeats.size() < seatsToSelect) {
                seatButton.setStyle("-fx-background-color: yellow; -fx-font-size: 10px; -fx-padding: 0;");
                seatButton.setPrefSize(30, 30);
                selectedSeats.add(seatButton);
            }
        } else if (seatButton.getStyle().contains("yellow")) {
            seatButton.setStyle("-fx-background-color: green; -fx-font-size: 10px; -fx-padding: 0;");
            seatButton.setPrefSize(30, 30);
            selectedSeats.remove(seatButton);
        }

        updateSeatNumbersField();
        updateConfirmButtonState();
    }

    private void updateSeatNumbersField() {
        StringBuilder seatNumbers = new StringBuilder();
        for (Button seat : selectedSeats) {
            if (seatNumbers.length() > 0) {
                seatNumbers.append(", ");
            }
            seatNumbers.append(seat.getText());
        }
        seatNumbersField.setText(seatNumbers.toString());
    }

    private void updateConfirmButtonState() {
        confirmButton.setDisable(selectedSeats.size() != seatsToSelect);
    }

@FXML
    private void goBackToDashboard(ActionEvent event) {
        SceneSwitcher.switchTo("customer_dashboard.fxml", "Customer Dashboard", event);
    }
    

@FXML
private void confirmBooking(ActionEvent event) {
    try {
        String name = nameField.getText();
        String dob = dobField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();
        String address = addressField.getText();
        int numSeats = Integer.parseInt(seatsDropdown.getValue());

        if (name.isEmpty() || dob.isEmpty() || phone.isEmpty() || email.isEmpty() || address.isEmpty()) {
            throw new IllegalArgumentException("All fields must be filled.");
        }

        String seatNumbers = seatNumbersField.getText();
        if (seatNumbers.isEmpty()) {
            throw new IllegalArgumentException("Please select seats.");
        }

        // Create a booking object
        Booking booking = new Booking(flightNumber, name, dob, phone, email, address, numSeats, userId, seatNumbers);

        // Add passengers' details
        for (int i = 0; i < numSeats - 1; i++) {
            VBox passengerCard = (VBox) passengerDetailsContainer.getChildren().get(i);
            // Card structure: [0]=header, [1]=separator, [2]=nameLabel, [3]=nameField, [4]=dobLabel, [5]=dobField
            String passengerName = ((TextField) passengerCard.getChildren().get(3)).getText();
            String passengerDob  = ((TextField) passengerCard.getChildren().get(5)).getText();

            if (passengerName.isEmpty() || passengerDob.isEmpty()) {
                throw new IllegalArgumentException("Please fill in all fields for passenger " + (i + 2));
            }

            booking.addPassenger(passengerName, passengerDob);
        }

        boolean success = BookingDAO.addBooking(booking);

        if (success) {
            System.out.println("Booking successful. Redirecting to ticket page...");

            // Save booking into context
            BookingContext.setCurrentBooking(booking);

            // Send booking confirmation notification
            NotificationService.getInstance().sendBookingConfirmation(booking);

            // Use SceneSwitcher to switch to ticket.fxml
            SceneSwitcher.switchTo("ticket.fxml", "Your Ticket", event);

        } else {
            showAlert("Booking Failed", "There was an issue with your booking. Please try again.");
        }

    } catch (NumberFormatException e) {
        showAlert("Invalid Input", "Number of seats must be a valid number.");
    } catch (IllegalArgumentException e) {
        showAlert("Invalid Input", e.getMessage());
    } catch (Exception e) {
        showAlert("Error", "Something went wrong. Please try again.");
        e.printStackTrace();
    }
}



    @FXML
    private void handlePayment(ActionEvent event) {
        // ── Step 1: Validate primary passenger fields ──────────────────────
        String name    = nameField.getText().trim();
        String dob     = dobField.getText().trim();
        String phone   = phoneField.getText().trim();
        String email   = emailField.getText().trim();
        String address = addressField.getText().trim();

        if (name.isEmpty() || dob.isEmpty() || phone.isEmpty() || email.isEmpty() || address.isEmpty()) {
            showAlert("Missing Details", "Please fill in all Personal Information fields before proceeding to payment.");
            return;
        }

        // ── Step 2: Validate seat selection ───────────────────────────────
        String seatNumbers = seatNumbersField.getText().trim();
        if (seatNumbers.isEmpty()) {
            showAlert("No Seats Selected", "Please select your seats from the seat map before proceeding to payment.");
            return;
        }

        // Make sure correct number of seats is selected
        if (seatsToSelect > 0 && selectedSeats.size() != seatsToSelect) {
            showAlert("Seat Mismatch", "You selected " + seatsToSelect + " seat(s) but only picked " + selectedSeats.size() + " on the map. Please complete seat selection.");
            return;
        }

        // ── Step 3: Validate extra passenger forms ────────────────────────
        if (seatsToSelect > 1) {
            for (int i = 0; i < seatsToSelect - 1; i++) {
                if (i >= passengerDetailsContainer.getChildren().size()) break;
                VBox card = (VBox) passengerDetailsContainer.getChildren().get(i);
                String pName = ((TextField) card.getChildren().get(3)).getText().trim();
                String pDob  = ((TextField) card.getChildren().get(5)).getText().trim();
                if (pName.isEmpty() || pDob.isEmpty()) {
                    showAlert("Missing Passenger Details", "Please fill in Name and Date of Birth for Passenger " + (i + 2) + ".");
                    return;
                }
            }
        }

        // ── Step 4: Fetch flight cost from DB ─────────────────────────────
        double costPerSeat = 0;
        String origin = "", destination = "", departureTime = "";
        try (java.sql.Connection conn = db.DBConnection.getConnection()) {
            java.sql.PreparedStatement ps = conn.prepareStatement(
                "SELECT cost, origin, destination, departure_time FROM flights WHERE flightNumber = ?");
            ps.setString(1, flightNumber);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                costPerSeat    = rs.getDouble("cost");
                origin         = rs.getString("origin");
                destination    = rs.getString("destination");
                departureTime  = rs.getString("departure_time");
            }
        } catch (Exception e) {
            showAlert("Error", "Could not fetch flight details: " + e.getMessage());
            return;
        }

        double totalCost = costPerSeat * seatsToSelect;

        // ── Step 5: Show Payment Summary Dialog ───────────────────────────
        Alert summary = new Alert(Alert.AlertType.CONFIRMATION);
        summary.setTitle("Payment Summary");
        summary.setHeaderText("✈ Review & Confirm Payment");

        String summaryText =
            "─────────────────────────────────\n" +
            "  FLIGHT DETAILS\n" +
            "─────────────────────────────────\n" +
            "  Flight No    : " + flightNumber + "\n" +
            "  Route        : " + origin + " → " + destination + "\n" +
            "  Departure    : " + departureTime + "\n\n" +
            "─────────────────────────────────\n" +
            "  BOOKING DETAILS\n" +
            "─────────────────────────────────\n" +
            "  Passenger    : " + name + "\n" +
            "  Seats        : " + seatNumbers + "\n" +
            "  No. of Seats : " + seatsToSelect + "\n\n" +
            "─────────────────────────────────\n" +
            "  PAYMENT DETAILS\n" +
            "─────────────────────────────────\n" +
            "  Cost per Seat: ₹" + String.format("%.2f", costPerSeat) + "\n" +
            "  No. of Seats : " + seatsToSelect + "\n" +
            "  ────────────────────────────\n" +
            "  TOTAL AMOUNT : ₹" + String.format("%.2f", totalCost) + "\n" +
            "─────────────────────────────────\n\n" +
            "Click OK to confirm payment.";

        summary.setContentText(summaryText);
        summary.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        // Style the dialog for readability
        summary.getDialogPane().setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");

        summary.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                // Mark payment as done
                payButton.setText("✔ Paid");
                payButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
                payButton.setDisable(true);
                paymentLabel.setText("✔ Payment of ₹" + String.format("%.2f", totalCost) + " Successful!");
                paymentLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold; -fx-font-size: 14px;");
                confirmButton.setDisable(false);
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
