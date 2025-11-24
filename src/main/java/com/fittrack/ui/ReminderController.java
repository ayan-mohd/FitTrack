package com.fittrack.ui;

import com.fittrack.app.UserSession;
import com.fittrack.db.DatabaseManager;
import com.fittrack.model.Reminder;
import com.fittrack.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ReminderController {

    @FXML private ComboBox<String> dayCombo;
    @FXML private ComboBox<String> workoutTypeCombo;
    @FXML private TextField customTypeField;
    @FXML private Button toggleTypeButton;
    @FXML private TextField timeField;
    @FXML private ListView<Reminder> reminderListView;

    private DatabaseManager dbManager = new DatabaseManager();
    private User currentUser;
    private ObservableList<Reminder> reminders;
    private boolean isCustomType = false;

    @FXML
    public void initialize() {
        UserSession session = UserSession.getInstance();
        if (session != null && session.getUser() != null) {
            currentUser = session.getUser();
            
            dayCombo.getItems().addAll("Everyday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
            workoutTypeCombo.getItems().addAll("Running", "Walking", "Cycling", "Swimming", "Gym", "Yoga", "HIIT");
            
            setupReminderList();
            loadReminders();
        }
    }

    @FXML
    private void handleToggleType() {
        isCustomType = !isCustomType;
        if (isCustomType) {
            workoutTypeCombo.setVisible(false);
            workoutTypeCombo.setManaged(false);
            customTypeField.setVisible(true);
            customTypeField.setManaged(true);
            toggleTypeButton.setText("List");
        } else {
            workoutTypeCombo.setVisible(true);
            workoutTypeCombo.setManaged(true);
            customTypeField.setVisible(false);
            customTypeField.setManaged(false);
            toggleTypeButton.setText("+");
        }
    }

    private void setupReminderList() {
        reminderListView.setCellFactory(param -> new ListCell<Reminder>() {
            private final Button toggleButton = new Button();
            private final Label infoLabel = new Label();
            private final HBox layout = new HBox(10, infoLabel, toggleButton);
            
            {
                layout.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(infoLabel, Priority.ALWAYS);
                infoLabel.setMaxWidth(Double.MAX_VALUE);
                
                toggleButton.setOnAction(event -> {
                    Reminder item = getItem();
                    if (item != null) {
                        boolean newState = !item.isActive();
                        if (dbManager.updateReminderStatus(item.getId(), newState)) {
                            item.setActive(newState);
                            updateButtonStyle(newState);
                        }
                    }
                });
            }

            private void updateButtonStyle(boolean active) {
                if (active) {
                    toggleButton.setText("ON");
                    toggleButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
                } else {
                    toggleButton.setText("OFF");
                    toggleButton.setStyle("-fx-background-color: gray; -fx-text-fill: white;");
                }
            }

            @Override
            protected void updateItem(Reminder item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    String day = item.getDay() != null ? item.getDay() : "Everyday";
                    infoLabel.setText(day + ": " + item.getWorkoutType() + " at " + item.getTime().toString());
                    
                    String color;
                    String type = item.getWorkoutType() != null ? item.getWorkoutType().toLowerCase() : "";
                    switch (type) {
                        case "gym": color = "#FFB74D"; break; // Light Orange
                        case "running": color = "#66BB6A"; break; // Light Green
                        case "walking": color = "#26A69A"; break; // Light Teal
                        case "cycling": color = "#42A5F5"; break; // Light Blue
                        case "swimming": color = "#64B5F6"; break; // Lighter Blue
                        case "yoga": color = "#AB47BC"; break; // Light Purple
                        case "hiit": color = "#EF5350"; break; // Light Red
                        default: color = "#E0E0E0"; break; // Light Gray
                    }
                    infoLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 14px;");
                    
                    updateButtonStyle(item.isActive());
                    setGraphic(layout);
                }
            }
        });
    }

    private void loadReminders() {
        List<Reminder> reminderList = dbManager.getReminders(currentUser.getId());
        reminders = FXCollections.observableArrayList(reminderList);
        reminderListView.setItems(reminders);
    }

    @FXML
    private void handleAddReminder() {
        String day = dayCombo.getValue();
        String type;
        
        if (isCustomType) {
            type = customTypeField.getText();
        } else {
            type = workoutTypeCombo.getValue();
        }
        
        String timeStr = timeField.getText();

        if (day == null || type == null || type.trim().isEmpty() || timeStr.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        try {
            LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
            
            Reminder reminder = new Reminder(currentUser.getId(), type, day, time, true);
            if (dbManager.addReminder(reminder)) {
                showAlert("Success", "Reminder added!");
                loadReminders();
                timeField.clear();
                customTypeField.clear();
            } else {
                showAlert("Error", "Failed to add reminder.");
            }
        } catch (DateTimeParseException e) {
            showAlert("Error", "Invalid time format. Use HH:mm (e.g., 14:30).");
        }
    }

    @FXML
    private void handleDeleteReminder() {
        int selectedIndex = reminderListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            Reminder reminder = reminders.get(selectedIndex);
            if (dbManager.deleteReminder(reminder.getId())) {
                loadReminders();
            } else {
                showAlert("Error", "Failed to delete reminder.");
            }
        } else {
            showAlert("Error", "Please select a reminder to delete.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
