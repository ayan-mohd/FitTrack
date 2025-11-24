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

public class ProfileController {

    @FXML private TextField ageField;
    @FXML private TextField weightField;
    @FXML private TextField heightField;
    @FXML private javafx.scene.control.Label bmiLabel;
    
    @FXML private TextField stepGoalField;
    
    @FXML private javafx.scene.control.Label totalStepsLabel;
    @FXML private javafx.scene.control.Label totalCaloriesLabel;
    @FXML private javafx.scene.control.Label totalDaysLabel;
    
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
            ageField.setText(String.valueOf(currentUser.getAge()));
            weightField.setText(String.valueOf(currentUser.getWeight()));
            heightField.setText(String.valueOf(currentUser.getHeight()));
            
            stepGoalField.setText(String.valueOf(currentUser.getDailyStepGoal()));
            
            // Load Total Stats
            int totalSteps = dbManager.getTotalSteps(currentUser.getId());
            totalStepsLabel.setText(String.valueOf(totalSteps));
            
            int totalCalories = dbManager.getTotalCaloriesBurned(currentUser.getId());
            totalCaloriesLabel.setText(String.valueOf(totalCalories));
            
            int totalDays = dbManager.getTotalWorkoutDays(currentUser.getId());
            totalDaysLabel.setText(String.valueOf(totalDays));
            
            // Calculate initial BMI if data exists
            handleCalculateBMI();
        }
    }

    @FXML
    private void handleSave() {
        try {
            int age = Integer.parseInt(ageField.getText());
            float weight = Float.parseFloat(weightField.getText());
            float height = Float.parseFloat(heightField.getText());
            
            int stepGoal = Integer.parseInt(stepGoalField.getText());

            currentUser.setAge(age);
            currentUser.setWeight(weight);
            currentUser.setHeight(height);
            currentUser.setDailyStepGoal(stepGoal);

            if (dbManager.updateUser(currentUser)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update profile.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numbers for age, weight, and height.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleAccountSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(FitTrackApp.class.getResource("/fxml/account.fxml"));
            Parent accountView = loader.load();
            
            BorderPane root = (BorderPane) ageField.getScene().getRoot();
            root.setCenter(accountView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCalculateBMI() {
        try {
            float weight = Float.parseFloat(weightField.getText());
            float heightCm = Float.parseFloat(heightField.getText());
            
            if (heightCm > 0) {
                float heightM = heightCm / 100;
                float bmi = weight / (heightM * heightM);
                bmiLabel.setText(String.format("%.1f", bmi));
                
                // Optional: Add category
                String category;
                if (bmi < 18.5) category = "(Underweight)";
                else if (bmi < 25) category = "(Normal)";
                else if (bmi < 30) category = "(Overweight)";
                else category = "(Obese)";
                
                bmiLabel.setText(String.format("%.1f %s", bmi, category));
            } else {
                bmiLabel.setText("-");
            }
        } catch (NumberFormatException e) {
            bmiLabel.setText("Invalid input");
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
