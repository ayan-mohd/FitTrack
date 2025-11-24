package com.fittrack.ui;

import com.fittrack.app.FitTrackApp;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

import java.io.IOException;

public class DeviceSetupController {

    @FXML
    private void handleConnectDevice() {
        showAlert(Alert.AlertType.INFORMATION, "Connect Device", "Searching for devices...");
    }

    @FXML
    private void handleSkip() throws IOException {
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
