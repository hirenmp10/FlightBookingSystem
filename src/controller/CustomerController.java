package controller;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import model.Flight;
import model.User;
import dao.FlightDAO;
import dao.UserDAO;
import utils.SceneSwitcher;
import utils.BookingContext;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import db.DBConnection;

import controller.ViewTicketsController;
import utils.BookingContext;

import java.util.List;

public class CustomerController {

    @FXML
    private VBox flightsContainer;

    @FXML
    public void initialize() {
        List<Flight> flights = FlightDAO.getApprovedFlights();

        for (Flight flight : flights) {
            // Flight card container
            VBox flightCard = new VBox(10);
            flightCard.setStyle(
                "-fx-background-color: #ffffff; " +
                "-fx-border-color: #cccccc; " +
                "-fx-border-radius: 12; " +
                "-fx-background-radius: 12; " +
                "-fx-padding: 20; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);"
            );
            flightCard.setAlignment(Pos.CENTER_LEFT);

            // Styling for labels
            String labelStyle = "-fx-font-size: 16px; -fx-font-weight: 500; -fx-text-fill: #333333;";

            Label flightLabel = new Label("✈ Flight: " + flight.getFlightNumber());
            flightLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2a2a2a;");

            Label routeLabel = new Label("From: " + flight.getOrigin() + " → To: " + flight.getDestination());
            routeLabel.setStyle(labelStyle);

            Label departureLabel = new Label("Departure: " + flight.getDepartureTime());
            departureLabel.setStyle(labelStyle);

            Label seatsCostLabel = new Label("Seats: " + flight.getAvailableSeats() + "/" + flight.getTotalSeats() +
                    "  |  Cost: ₹" + flight.getCost());
            seatsCostLabel.setStyle(labelStyle);

            // Book button
            Button bookBtn = new Button("Book Now");
            bookBtn.setStyle(
                "-fx-background-color: #4C8BF5; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 10 20; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8;"
            );
            bookBtn.setOnAction(e -> goToBooking(e, flight.getFlightNumber()));

            // Add everything to the card
            flightCard.getChildren().addAll(flightLabel, routeLabel, departureLabel, seatsCostLabel, bookBtn);

            // Add card to the main container
            flightsContainer.getChildren().add(flightCard);
        }
    }

    private void goToBooking(ActionEvent event, String flightNumber) {
        BookingContext.setSelectedFlightNumber(flightNumber);
        System.out.println("Selected Flight Number set to: " + flightNumber); // Debugging line
        SceneSwitcher.switchTo("book.fxml", "Book Flight", event);
    }

    @FXML
    private void handleViewTickets(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/viewtickets.fxml"));
            Parent root = loader.load();

            controller.ViewTicketsController controller = loader.getController();
            controller.setDashboardMode(true, utils.BookingContext.getLoggedInUserId()); // user ID from context

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("My Tickets");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleViewProfile(ActionEvent event) {
        // Get current user data from database
        int userId = BookingContext.getLoggedInUserId();
        User user = getUserById(userId);
        
        if (user == null) {
            System.err.println("Error: Could not retrieve user profile");
            return;
        }
        
        // Create a new stage for the popup
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initOwner(((Node) event.getSource()).getScene().getWindow());
        popupStage.setTitle("My Profile");
        
        // Create the grid layout
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        // Style constants
        String labelStyle = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;";
        String textFieldStyle = "-fx-font-size: 14px; -fx-background-radius: 6;";
        String buttonStyle = "-fx-background-color: #4C8BF5; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;";
        
        // Components
        Label idLabel = new Label("User ID:");
        idLabel.setStyle(labelStyle);
        Label idValue = new Label(String.valueOf(user.getId()));
        
        Label usernameLabel = new Label("Username:");
        usernameLabel.setStyle(labelStyle);
        TextField usernameField = new TextField(user.getUsername());
        usernameField.setStyle(textFieldStyle);
        
        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle(labelStyle);
        PasswordField passwordField = new PasswordField();
        passwordField.setText(user.getPassword());
        passwordField.setStyle(textFieldStyle);
        
        Button saveButton = new Button("Save Changes");
        saveButton.setStyle(buttonStyle);
        saveButton.setOnAction(e -> {
            String newUsername = usernameField.getText().trim();
            String newPassword = passwordField.getText();
            if (newUsername.isEmpty() || newPassword.isEmpty()) {
                showErrorMessage(popupStage, "Fields cannot be empty!");
                return;
            }
            if (updateUserProfile(user.getId(), newUsername, newPassword)) {
                popupStage.close();
            } else {
                showErrorMessage(popupStage, "Failed to update profile.");
            }
        });
        
        grid.add(idLabel, 0, 0);
        grid.add(idValue, 1, 0);
        grid.add(usernameLabel, 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordBox(passwordField), 1, 2);
        grid.add(saveButton, 1, 3);
        
        Scene scene = new Scene(grid, 400, 250);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }

    private HBox passwordBox(PasswordField passwordField) {
        HBox box = new HBox(10);
        TextField visibleField = new TextField(passwordField.getText());
        visibleField.setManaged(false);
        visibleField.setVisible(false);
        
        Button toggle = new Button("Show");
        toggle.setOnAction(e -> {
            if (passwordField.isVisible()) {
                visibleField.setText(passwordField.getText());
                passwordField.setVisible(false); passwordField.setManaged(false);
                visibleField.setVisible(true); visibleField.setManaged(true);
                toggle.setText("Hide");
            } else {
                passwordField.setText(visibleField.getText());
                visibleField.setVisible(false); visibleField.setManaged(false);
                passwordField.setVisible(true); passwordField.setManaged(true);
                toggle.setText("Show");
            }
        });
        box.getChildren().addAll(passwordField, visibleField, toggle);
        return box;
    }

    private User getUserById(int userId) {
        return UserDAO.getUserById(userId);
    }
    
    private boolean updateUserProfile(int userId, String username, String password) {
        return UserDAO.updateUser(userId, username, password);
    }
    
    private void showErrorMessage(Stage owner, String message) {
        Stage errorStage = new Stage();
        errorStage.initModality(Modality.APPLICATION_MODAL);
        errorStage.initOwner(owner);
        errorStage.setTitle("Error");
        VBox vbox = new VBox(20, new Label(message), new Button("OK"));
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));
        ((Button)vbox.getChildren().get(1)).setOnAction(e -> errorStage.close());
        errorStage.setScene(new Scene(vbox, 300, 150));
        errorStage.showAndWait();
    }
    
    @FXML
    private void handleLogout(ActionEvent event) {
        SceneSwitcher.switchTo("login.fxml", "Login", event);
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        SceneSwitcher.switchTo("search.fxml", "Search Flights", event);
    }
}