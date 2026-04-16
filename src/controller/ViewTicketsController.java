package controller;
import db.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import utils.SceneSwitcher;

public class ViewTicketsController {
    @FXML private VBox ticketDisplayBox;
    @FXML private TextField bookingIdField;
    @FXML private TextArea ticketText;
    @FXML private Button cancelBtn;
    @FXML private Button goBackButton;
    @FXML private Button goDashboardButton;
    
    private boolean isDashboardMode = false;
    private int userId = -1;
    private int selectedBookingId = -1;
    private TextArea selectedTicketArea = null;
    
    @FXML
    public void initialize() {
        // Hide dashboard button by default (will show only in dashboardMode)
        if (goDashboardButton != null) {
            goDashboardButton.setVisible(false);
        }
    }
    
    public void setDashboardMode(boolean value, int userId) {
        this.isDashboardMode = value;
        this.userId = userId;
        
        // Configure UI based on mode
        if (bookingIdField != null) {
            bookingIdField.setVisible(!value);
        }
        
         //Configure back button behavior
        if (goBackButton != null) {
            goBackButton.setText(value ? "Back to Dashboard" : "Back to Login");
        }
        
        // Show/hide dashboard button based on mode
        //if (goDashboardButton != null) {
         //   goDashboardButton.setVisible(value); // Only show in dashboard mode
       // }
        
        // Load tickets automatically if in dashboard mode
        if (value) {
            loadTickets();
        }
    }
    
    @FXML
    public void loadTickets() {
        ticketDisplayBox.getChildren().clear();
        selectedBookingId = -1; // Reset selection
        selectedTicketArea = null;
        
        List<model.Booking> bookings;
        if (isDashboardMode) {
            bookings = dao.BookingDAO.getBookingsByUserId(userId);
        } else {
            String idText = bookingIdField.getText().trim();
            if (idText.isEmpty()) {
                ticketText.setText("Please enter a booking ID.");
                return;
            }
            try {
                int bid = Integer.parseInt(idText);
                model.Booking b = dao.BookingDAO.getBookingById(bid);
                bookings = new ArrayList<>();
                if (b != null) bookings.add(b);
            } catch (NumberFormatException e) {
                ticketText.setText("Please enter a valid booking ID.");
                return;
            }
        }

        if (bookings.isEmpty()) {
            ticketText.setText(isDashboardMode ? "You have no bookings." : "No booking found with ID: " + bookingIdField.getText());
            return;
        }

        for (model.Booking b : bookings) {
            final int bid = b.getBookingId();
            model.Flight f = dao.FlightDAO.getFlightByNumber(b.getFlightNumber());
            TextArea ta = new TextArea(formatTicket(b, f));
            ta.setEditable(false);
            ta.setPrefHeight(200);
            ta.setWrapText(true);

            if (isDashboardMode) {
                ta.setStyle("-fx-border-color: transparent;");
                ta.setOnMouseClicked(event -> {
                    if (selectedTicketArea != null) {
                        selectedTicketArea.setStyle("-fx-border-color: transparent;");
                    }
                    selectedBookingId = bid;
                    selectedTicketArea = ta;
                    ta.setStyle("-fx-border-color: #1976D2; -fx-border-width: 2px;");
                    ticketText.setText("Selected Booking ID: " + bid);
                });
            }
            ticketDisplayBox.getChildren().add(ta);
        }
        if (!isDashboardMode) ticketText.setText("");
    }
    
    @FXML
    public void cancelBooking() {
        int bookingId;
        try {
            if (isDashboardMode) {
                if (selectedBookingId != -1) {
                    bookingId = selectedBookingId;
                } else {
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Cancel Booking");
                    dialog.setHeaderText("Enter Booking ID to cancel");
                    dialog.setContentText("Booking ID:");
                    Optional<String> result = dialog.showAndWait();
                    if (result.isEmpty()) return;
                    bookingId = Integer.parseInt(result.get());
                }
            } else {
                if (bookingIdField.getText().isEmpty()) {
                    ticketText.setText("Please enter a booking ID first.");
                    return;
                }
                bookingId = Integer.parseInt(bookingIdField.getText());
            }

            model.Booking booking = dao.BookingDAO.getBookingById(bookingId);
            if (booking == null) {
                ticketText.setText("Booking ID not found.");
                return;
            }

            if (isDashboardMode && booking.getUserId() != userId) {
                ticketText.setText("This booking doesn't belong to your account.");
                return;
            }

            if (!isDashboardMode) {
                TextInputDialog userIdDialog = new TextInputDialog();
                userIdDialog.setTitle("User Verification");
                userIdDialog.setHeaderText("Security Verification");
                userIdDialog.setContentText("Please enter your User ID:");
                Optional<String> userIdResult = userIdDialog.showAndWait();
                if (userIdResult.isEmpty()) return;
                if (Integer.parseInt(userIdResult.get()) != booking.getUserId()) {
                    ticketText.setText("User ID and Booking ID don't match.");
                    return;
                }
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to cancel booking ID " + bookingId + "?", ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText("Cancel Booking Confirmation");
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    int seatsToReturn = booking.getSeatNumbers() != null ? booking.getSeatNumbers().split(",").length : booking.getNumSeats();
                    if (dao.BookingDAO.cancelBooking(bookingId, booking.getFlightNumber(), seatsToReturn)) {
                        ticketText.setText("Booking #" + bookingId + " has been cancelled successfully.");
                        selectedBookingId = -1;
                        selectedTicketArea = null;
                        loadTickets();
                    } else {
                        ticketText.setText("Failed to cancel booking.");
                    }
                }
            });
        } catch (NumberFormatException e) {
            ticketText.setText("Invalid input.");
        }
    }
    
    @FXML
    private void goBack(ActionEvent event) {
        if (isDashboardMode) {
            SceneSwitcher.switchTo("customer_dashboard.fxml", "Customer Dashboard", event);
        } else {
            SceneSwitcher.switchTo("login.fxml", "Login", event);
        }
    }
    
    @FXML
    private void goToDashboard(ActionEvent event) {
        SceneSwitcher.switchTo("customer_dashboard.fxml", "Customer Dashboard", event);
    }
    
    private String formatTicket(model.Booking b, model.Flight f) {
        double costPerSeat = f != null ? f.getCost() : 0;
        int seatCount = b.getSeatNumbers() != null ? b.getSeatNumbers().split(",").length : b.getNumSeats();
        double totalCost = costPerSeat * seatCount;
        
        StringBuilder ticket = new StringBuilder();
        ticket.append("====== FLIGHT TICKET ======\n\n")
              .append("Booking ID: ").append(b.getBookingId()).append("\n")
              .append("Flight: ").append(b.getFlightNumber()).append("\n")
              .append("Route: ").append(f != null ? f.getOrigin() + " → " + f.getDestination() : "Unknown").append("\n")
              .append("Name: ").append(b.getUserName()).append("\n")
              .append("Email: ").append(b.getEmail()).append("\n")
              .append("Seats: ").append(b.getSeatNumbers() != null ? b.getSeatNumbers() : "None").append("\n")
              .append("Passengers: ").append(b.getExtraPassengers() != null ? b.getExtraPassengers() : "None").append("\n")
              .append("Departure: ").append(f != null ? f.getDepartureTime() : "Unknown").append("\n")
              .append("Base Cost: ₹").append(String.format("%.2f", costPerSeat)).append("\n")
              .append("Number of Seats: ").append(seatCount).append("\n")
              .append("Total Cost: ₹").append(String.format("%.2f", totalCost)).append("\n")
              .append("\n==========================");
        
        return ticket.toString();
    }
}