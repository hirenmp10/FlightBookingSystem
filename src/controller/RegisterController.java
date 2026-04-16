package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.User;
import dao.UserDAO;
import utils.SceneSwitcher;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private String role = "customer"; // Default role

    public void setRole(String role) {
        this.role = role;
    }

    @FXML
    private void handleRegisterRedirect(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Username, email, and password cannot be empty.");
            return;
        }

        if (!utils.ValidationUtils.isValidEmail(email)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a valid email address.");
            return;
        }

        User user = utils.UserFactory.createUser(username, password, role);
        user.setEmail(email);
        boolean success = UserDAO.addUser(user);

        if (success) {
            service.NotificationService.getInstance().sendRegistrationConfirmation(user);
            SceneSwitcher.switchTo("login.fxml", "Login", event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Registration Failed", "User already exists or registration failed.");
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        SceneSwitcher.switchTo("login.fxml", "Login", event);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
