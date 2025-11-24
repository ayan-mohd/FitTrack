package com.fittrack.ui;

import com.fittrack.app.FitTrackApp;
import com.fittrack.app.UserSession;
import com.fittrack.db.DatabaseManager;
import com.fittrack.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.sql.SQLException;

public class AccountController {

    @FXML private TextField emailField;
    @FXML private TextField nameField;

    private DatabaseManager dbManager = new DatabaseManager();
    private User currentUser;

    @FXML
    public void initialize() {
        UserSession session = UserSession.getInstance();
        if (session != null && session.getUser() != null) {
            currentUser = session.getUser();
            loadUserData();
        } else {
            try {
                FitTrackApp.setRoot("login");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadUserData() {
        if (currentUser != null) {
            nameField.setText(currentUser.getName());
            emailField.setText(currentUser.getEmail());
        }
    }

    @FXML
    private void handleSave() {
        try {
            String name = nameField.getText();

            if (name.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Name cannot be empty.");
                return;
            }

            currentUser.setName(name);

            if (dbManager.updateUser(currentUser)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Account updated successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update account.");
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                showAlert(Alert.AlertType.ERROR, "Error", "Name already taken.");
            } else {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(FitTrackApp.class.getResource("/fxml/profile.fxml"));
            Parent profileView = loader.load();
            
            BorderPane root = (BorderPane) emailField.getScene().getRoot();
            root.setCenter(profileView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() throws IOException {
        UserSession.cleanUserSession();
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
