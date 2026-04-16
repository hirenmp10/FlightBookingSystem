package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import utils.AlertUtils;
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
    @FXML private TextField countryCodeField;
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

        // Prepopulate user details into booking form
        model.User loggedInUser = dao.UserDAO.getUserById(userId);
        if (loggedInUser != null) {
            String existingEmail = loggedInUser.getEmail();
            if (existingEmail != null && !existingEmail.isEmpty() && !"null".equalsIgnoreCase(existingEmail)) {
                emailField.setText(existingEmail);
            }
            // Defaulting name text to the user's username for convenience
            if (loggedInUser.getUsername() != null && !loggedInUser.getUsername().isEmpty()) {
                nameField.setText(loggedInUser.getUsername());
            }
        }

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
        int colsPerRow = 6;  // 6 seats across: A, B, C [Aisle] D, E, F
        int numRows = (totalSeats + colsPerRow - 1) / colsPerRow;

        seatLayoutContainer.getChildren().clear();
        seatLayoutContainer.setSpacing(20);

        // --- Cockpit / Front Indicator ---
        VBox cockpit = new VBox(5);
        cockpit.setAlignment(javafx.geometry.Pos.CENTER);
        Label cockpitIcon = new Label("👨‍✈️");
        cockpitIcon.setStyle("-fx-font-size: 24px;");
        Label cockpitLabel = new Label("COCKPIT / FRONT");
        cockpitLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4C8BF5;");
        cockpit.getChildren().addAll(cockpitIcon, cockpitLabel);
        seatLayoutContainer.getChildren().add(cockpit);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setAlignment(javafx.geometry.Pos.CENTER);

        // Column Labels (A B C [Aisle] D E F)
        String[] colLabels = {"A", "B", "C", "", "D", "E", "F"};
        for (int c = 0; c < colLabels.length; c++) {
            if (!colLabels[c].isEmpty()) {
                Label l = new Label(colLabels[c]);
                l.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #777;");
                grid.add(l, c + 1, 0); // Start at col index 1 because index 0 is for row numbers
                javafx.geometry.HPos hpos = javafx.geometry.HPos.CENTER;
                GridPane.setHalignment(l, hpos);
            }
        }

        for (int r = 0; r < numRows; r++) {
            // Row Number Label
            Label rowNum = new Label(String.valueOf(r + 1));
            rowNum.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #777; -fx-padding: 0 10 0 0;");
            grid.add(rowNum, 0, r + 1);

            for (int c = 0; c < 6; c++) {
                int seatIdx = (r * 6) + c;
                if (seatIdx >= totalSeats) break;

                int seatNum = seatIdx + 1;
                Button seatButton = new Button(String.valueOf(seatNum));
                
                // --- SEAT STYLING ---
                seatButton.setPrefSize(45, 40);
                seatButton.setMinSize(45, 40);
                
                String baseStyle = "-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;";
                
                if (BookingDAO.isSeatBooked(flightNumber, seatNum)) {
                    seatButton.setStyle(baseStyle + "-fx-background-color: #e74c3c; -fx-text-fill: white;");
                    seatButton.setDisable(true);
                } else {
                    seatButton.setStyle(baseStyle + "-fx-background-color: #2ecc71; -fx-text-fill: white;");
                    seatButton.setOnAction(e -> handleSeatSelectionAction(seatButton, seatNum));
                }

                // If column is D, E, F, we shift the index by 1 to leave room for the Aisle
                int gridCol = (c < 3) ? (c + 1) : (c + 2);
                grid.add(seatButton, gridCol, r + 1);
            }

            // Aisle Label (Empty or subtly styled)
            if (r == 0) {
                 Label aisleLabel = new Label("AISLE");
                 aisleLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #aaa; -fx-rotate: 90;");
                 grid.add(aisleLabel, 4, 1, 1, numRows);
                 GridPane.setHalignment(aisleLabel, javafx.geometry.HPos.CENTER);
                 GridPane.setValignment(aisleLabel, javafx.geometry.VPos.CENTER);
            }
        }

        seatLayoutContainer.getChildren().add(grid);

        // --- Back of Plane Indicator ---
        VBox tail = new VBox(5);
        tail.setAlignment(javafx.geometry.Pos.CENTER);
        Label tailLabel = new Label("REAR OF PLANE / GALLEY");
        tailLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #555;");
        tail.getChildren().add(tailLabel);
        seatLayoutContainer.getChildren().add(tail);
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
            passengerDob.setPromptText("Enter DOB: YYYY-MM-DD");
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
        if (seatButton.getStyle().contains("#e74c3c")) { // Red
            return; // Booked seat, can't select
        }

        String baseStyle = "-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;";

        if (seatButton.getStyle().contains("#2ecc71")) { // Green
            if (selectedSeats.size() < seatsToSelect) {
                seatButton.setStyle(baseStyle + "-fx-background-color: #f1c40f; -fx-text-fill: black;"); // Yellow
                selectedSeats.add(seatButton);
            }
        } else if (seatButton.getStyle().contains("#f1c40f")) { // Yellow
            seatButton.setStyle(baseStyle + "-fx-background-color: #2ecc71; -fx-text-fill: white;"); // Green
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
        String name = nameField.getText().trim();
        String dob = dobField.getText().trim();
        String phoneStr = (countryCodeField.getText().trim() + phoneField.getText().trim()).replaceAll("\\s", "");
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        int numSeats = Integer.parseInt(seatsDropdown.getValue());

        if (name.isEmpty() || dob.isEmpty() || countryCodeField.getText().isEmpty() || phoneField.getText().isEmpty() || email.isEmpty() || address.isEmpty()) {
            throw new IllegalArgumentException("All fields must be filled.");
        }

        String seatNumbers = seatNumbersField.getText();
        if (seatNumbers.isEmpty()) {
            throw new IllegalArgumentException("Please select seats.");
        }

        // Create a booking object
        Booking booking = new Booking(flightNumber, name, dob, phoneStr, email, address, numSeats, userId, seatNumbers);

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
            AlertUtils.showError("Booking Failed", "There was an issue with your booking. Please try again.");
        }

    } catch (NumberFormatException e) {
        AlertUtils.showError("Invalid Input", "Number of seats must be a valid number.");
    } catch (IllegalArgumentException e) {
        AlertUtils.showError("Invalid Input", e.getMessage());
    } catch (Exception e) {
        AlertUtils.showError("Error", "Something went wrong. Please try again.");
        e.printStackTrace();
    }
}



    @FXML
    private void handlePayment(ActionEvent event) {
        // ── Step 1: Validate primary passenger fields ──────────────────────
        String name    = nameField.getText().trim();
        String dob     = dobField.getText().trim();
        String phonePart = phoneField.getText().trim();
        String countryPart = countryCodeField.getText().trim();
        String fullPhone = (countryPart + phonePart).replaceAll("\\s", "");
        String email   = emailField.getText().trim();
        String address = addressField.getText().trim();

        if (name.isEmpty() || dob.isEmpty() || countryPart.isEmpty() || phonePart.isEmpty() || email.isEmpty() || address.isEmpty()) {
            AlertUtils.showError("Missing Details", "Please fill in all Personal Information fields before proceeding to payment.");
            return;
        }

        if (!utils.ValidationUtils.isValidEmail(email)) {
            AlertUtils.showError("Validation Error", "Please enter a valid email address.");
            return;
        }

        if (!utils.ValidationUtils.isValidPhone(fullPhone)) {
            AlertUtils.showError("Format Error", "Please enter a valid phone number. Country code started with + and then digits.");
            return;
        }

        // ── Step 2: Validate seat selection ───────────────────────────────
        String seatNumbers = seatNumbersField.getText().trim();
        if (seatNumbers.isEmpty()) {
            AlertUtils.showError("No Seats Selected", "Please select your seats from the seat map before proceeding to payment.");
            return;
        }

        // Make sure correct number of seats is selected
        if (seatsToSelect > 0 && selectedSeats.size() != seatsToSelect) {
            AlertUtils.showError("Seat Mismatch", "You selected " + seatsToSelect + " seat(s) but only picked " + selectedSeats.size() + " on the map. Please complete seat selection.");
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
                    AlertUtils.showError("Missing Passenger Details", "Please fill in Name and Date of Birth for Passenger " + (i + 2) + ".");
                    return;
                }
            }
        }

        // ── Step 4: Fetch flight details from DAO ─────────────────────────────
        double costPerSeat = 0;
        String origin = "", destination = "", departureTime = "";
        model.Flight flight = dao.FlightDAO.getFlightByNumber(flightNumber);
        
        if (flight != null) {
            costPerSeat    = flight.getCost();
            origin         = flight.getOrigin();
            destination    = flight.getDestination();
            departureTime  = flight.getDepartureTime().toString();
        } else {
            AlertUtils.showError("Error", "Could not fetch flight details.");
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
}
