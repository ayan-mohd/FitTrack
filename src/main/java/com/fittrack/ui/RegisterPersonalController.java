package com.fittrack.ui;

import com.fittrack.app.FitTrackApp;
import com.fittrack.app.RegistrationContext;
import com.fittrack.db.DatabaseManager;
import com.fittrack.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;

public class RegisterPersonalController {

    @FXML private TextField usernameField;
    @FXML private TextField ageField;
    @FXML private TextField weightField;
    @FXML private TextField heightField;
    @FXML private ComboBox<String> sexField;

    private DatabaseManager dbManager = new DatabaseManager();

    @FXML
    public void initialize() {
        sexField.getItems().addAll("Male", "Female", "Other");
    }

    @FXML
    private void handleRegister() {
        try {
            String name = usernameField.getText();
            int age = Integer.parseInt(ageField.getText());
            float weight = Float.parseFloat(weightField.getText());
            float height = Float.parseFloat(heightField.getText());
            String sex = sexField.getValue();

            if (name.isEmpty() || sex == null) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "All fields are required.");
                return;
            }

            RegistrationContext context = RegistrationContext.getInstance();
            String email = context.getEmail();
            String password = context.getPassword();

            if (email == null || password == null) {
                showAlert(Alert.AlertType.ERROR, "Session Error", "Registration session expired. Please start over.");
                FitTrackApp.setRoot("register");
                return;
            }

            User user = new User(name, email, password);
            user.setAge(age);
            user.setWeight(weight);
            user.setHeight(height);
            user.setSex(sex);

            if (dbManager.registerUser(user)) {
                context.clear();
                // showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "You can now login.");
                FitTrackApp.setRoot("device_setup");
            } else {
                showAlert(Alert.AlertType.ERROR, "Registration Failed", "Could not register user.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numbers for age, weight, and height.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() throws IOException {
        FitTrackApp.setRoot("register");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
