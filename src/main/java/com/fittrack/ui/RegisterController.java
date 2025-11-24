package com.fittrack.ui;

import com.fittrack.app.FitTrackApp;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class RegisterController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleNext() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Email and Password are required.");
            return;
        }

        com.fittrack.app.RegistrationContext.getInstance().setCredentials(email, password);

        try {
            FitTrackApp.setRoot("register_personal");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load next step.");
        }
    }

    @FXML
    private void handleBack() throws IOException {
        FitTrackApp.setRoot("login");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
