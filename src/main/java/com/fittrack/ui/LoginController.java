package com.fittrack.ui;

import com.fittrack.app.FitTrackApp;
import com.fittrack.app.UserSession;
import com.fittrack.db.DatabaseManager;
import com.fittrack.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;
    
    @FXML
    private TextField passwordTextField;
    
    @FXML
    private Button togglePasswordButton;

    private DatabaseManager dbManager = new DatabaseManager();
    
    @FXML
    public void initialize() {
        // Bind text properties so they always have the same value
        if (passwordTextField != null && passwordField != null) {
            passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        }
    }
    
    @FXML
    private void handleTogglePassword() {
        boolean isVisible = passwordTextField.isVisible();
        passwordTextField.setVisible(!isVisible);
        passwordField.setVisible(isVisible);
        
        if (!isVisible) {
             togglePasswordButton.setText("🙈");
        } else {
             togglePasswordButton.setText("👁");
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        // In a real app, hash the password before sending to DB
        // String passwordHash = HashUtil.hash(password); 
        // For Phase 1 prototype, we might just use plain text or simple hash
        
        if (dbManager.validateLogin(email, password)) {
            System.out.println("Login validation successful for: " + email);
            // showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Welcome back!");
            try {
                User user = dbManager.getUserByEmail(email);
                if (user != null) {
                    System.out.println("User retrieved: " + user.getName());
                    UserSession.setSession(user);
                    FitTrackApp.setRoot("dashboard");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Login Error", "Could not retrieve user details.");
                }
            } catch (IOException e) {
                System.err.println("Error loading dashboard:");
                e.printStackTrace();
            }
        } else {
            System.out.println("Login validation failed.");
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid email or password.");
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FitTrackApp.setRoot("register");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
