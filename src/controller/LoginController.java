package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import utils.AlertUtils;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import model.User;
import dao.UserDAO;
import utils.SceneSwitcher;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import javafx.event.ActionEvent;
import utils.ViewFactory;
import utils.View;


import utils.BookingContext;


public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;


@FXML
private void handleLogin(ActionEvent event) {
    String username = usernameField.getText();
    String password = passwordField.getText();

    if (username.isEmpty() || password.isEmpty()) {
        AlertUtils.showError("Input Required", "Please enter both username and password.");
        return;
    }

    // Database login check
    User user = UserDAO.getUser(username, password);
    if (user != null) {
        // Set the logged-in user's ID in BookingContext
        BookingContext.setLoggedInUserId(user.getId());

        // Use the ViewFactory to redirect based on user role
        try {
            View view = ViewFactory.getView(user.getRole());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(view.getScene());
            stage.setTitle(view.getTitle());
            stage.setMaximized(true);
            stage.setFullScreenExitHint("");
            stage.setFullScreen(true);
            stage.show();
        } catch (Exception e) {
            AlertUtils.showError("Login Failed", e.getMessage());
        }
    } else {
        AlertUtils.showError("Login Failed", "Invalid credentials. Try again.");
    }
}



@FXML
private void handleViewTickets(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/viewtickets.fxml"));
        Parent root = loader.load();

        controller.ViewTicketsController controller = loader.getController();
        controller.setDashboardMode(false, -1); // login-side mode

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Find Ticket by ID");
        stage.setFullScreenExitHint("");  // ← this line hides ESC message
        stage.setFullScreen(true);
        stage.show();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
    

    @FXML
    private void handleRegisterRedirect(ActionEvent event) {
        SceneSwitcher.switchTo("register.fxml", "Register", event);
    }
}
