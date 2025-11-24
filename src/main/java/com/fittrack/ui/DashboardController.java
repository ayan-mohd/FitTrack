package com.fittrack.ui;

import com.fittrack.app.FitTrackApp;
import com.fittrack.app.UserSession;
import com.fittrack.db.DatabaseManager;
import com.fittrack.model.Meal;
import com.fittrack.model.Reminder;
import com.fittrack.model.User;
import com.fittrack.model.Workout;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    @FXML private BorderPane mainLayout;
    @FXML private ScrollPane homeScrollPane;

    @FXML private Label welcomeLabel;
    @FXML private Button profileButton;
    @FXML private Label distanceLabel;
    @FXML private Label workoutCountLabel;
    @FXML private Label totalCaloriesLabel;
    @FXML private Button btnHome;
    @FXML private Button btnReminder;
    @FXML private Button btnYou;
    
    // Step Goal Elements
    @FXML private Label stepGoalLabel;
    @FXML private ProgressBar stepProgressBar;
    @FXML private Label stepProgressLabel;
    
    @FXML private ListView<Reminder> activeRemindersListView;
    
    // Recent Activity Labels
    @FXML private Label recentStepsLabel;
    @FXML private Label recentDistanceLabel;
    @FXML private Label streakLabel;
    @FXML private Label recentCaloriesLabel;
    
    @FXML private DatePicker workoutDate;
    @FXML private ComboBox<String> workoutType;
    @FXML private TextField workoutDuration;
    @FXML private TextField workoutCalories;
    @FXML private TextField workoutNotes;
    @FXML private Button addWorkoutButton;
    @FXML private Button updateWorkoutButton;

    @FXML private TableView<Workout> workoutTable;
    @FXML private TableColumn<Workout, LocalDate> colDate;
    @FXML private TableColumn<Workout, String> colType;
    @FXML private TableColumn<Workout, Integer> colDuration;
    @FXML private TableColumn<Workout, Integer> colCalories;
    @FXML private TableColumn<Workout, String> colNotes;

    // Meal UI Elements
    @FXML private DatePicker mealDate;
    @FXML private ComboBox<String> mealType;
    @FXML private TextField foodItem;
    @FXML private TextField mealCalories;
    @FXML private TableView<Meal> mealTable;
    @FXML private TableColumn<Meal, LocalDate> colMealDate;
    @FXML private TableColumn<Meal, String> colMealType;
    @FXML private TableColumn<Meal, String> colFoodItem;
    @FXML private TableColumn<Meal, Integer> colMealCalories;

    private DatabaseManager dbManager = new DatabaseManager();
    private User currentUser;
    private Workout selectedWorkoutForEdit;

    @FXML
    public void initialize() {
        System.out.println("DashboardController initializing...");
        UserSession session = UserSession.getInstance();
        if (session != null && session.getUser() != null) {
            currentUser = session.getUser();
            System.out.println("Current user: " + currentUser.getName());
            welcomeLabel.setText("Welcome, " + currentUser.getName() + "!");
            
            if (currentUser.getName() != null && !currentUser.getName().isEmpty()) {
                profileButton.setText(currentUser.getName().substring(0, 1).toUpperCase());
            }
            
            try {
                setupTable();
                setupRemindersList();
                loadDashboardData();
                setActiveTab(btnHome);
            } catch (Exception e) {
                System.err.println("Error loading dashboard data:");
                e.printStackTrace();
            }
        } else {
            System.out.println("No user session found. Redirecting to login...");
            try {
                FitTrackApp.setRoot("login");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        workoutDate.setValue(LocalDate.now());
        workoutType.getItems().addAll("Running", "Walking", "Cycling", "Swimming", "Gym", "Yoga", "HIIT", "Pilates", "Strength Training", "Cardio");
        
        // Initialize Meal UI
        mealDate.setValue(LocalDate.now());
        mealType.getItems().addAll("Breakfast", "Lunch", "Dinner", "Snack");
        
        System.out.println("DashboardController initialization complete.");
    }

    private void setupTable() {
        colDate.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("date"));
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colDate.setCellFactory(column -> new TableCell<Workout, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        colType.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("type"));
        colDuration.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("durationMinutes"));
        colCalories.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("caloriesBurned"));
        colNotes.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("notes"));

        // Setup Meal Table
        colMealDate.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("date"));
        colMealType.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("mealType"));
        colFoodItem.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("foodItem"));
        colMealCalories.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("calories"));
    }

    private void setupRemindersList() {
        activeRemindersListView.setCellFactory(param -> new ListCell<Reminder>() {
            @Override
            protected void updateItem(Reminder item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String day = item.getDay() != null ? item.getDay() : "";
                    String time = item.getTime() != null ? item.getTime().toString() : "";
                    String type = item.getWorkoutType() != null ? item.getWorkoutType() : "Reminder";
                    
                    setText(type + " - " + day + " " + time);
                    
                    String color;
                    switch (type.toLowerCase()) {
                        case "gym": color = "#FF8C00"; break; // Dark Orange
                        case "running": color = "green"; break;
                        case "walking": color = "teal"; break;
                        case "cycling": color = "blue"; break;
                        case "swimming": color = "darkblue"; break;
                        case "yoga": color = "purple"; break;
                        case "hiit": color = "red"; break;
                        default: color = "black"; break;
                    }
                    
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-padding: 5px; -fx-background-color: transparent;");
                }
            }
        });
    }

    private void loadDashboardData() {
        if (currentUser == null) return;

        // Load Steps
        int steps = dbManager.getSteps(currentUser.getId(), LocalDate.now());

        // Calculate and Load Distance (approx 0.0008 km per step)
        double distance = steps * 0.0008;
        distanceLabel.setText(String.format("%.2f", distance));

        // Load Stats (Today)
        int workoutCount = dbManager.getWorkoutCountToday(currentUser.getId());
        workoutCountLabel.setText(String.valueOf(workoutCount));

        int totalCalories = dbManager.getCaloriesBurnedToday(currentUser.getId());
        totalCaloriesLabel.setText(String.valueOf(totalCalories));
        
        // Load Step Goal Progress
        int stepGoal = currentUser.getDailyStepGoal();
        if (stepGoal <= 0) stepGoal = 10000; // Default
        
        stepGoalLabel.setText(String.format("%,d", stepGoal));
        
        double progress = (stepGoal > 0) ? (double) steps / stepGoal : 0;
        if (progress > 1.0) progress = 1.0;
        stepProgressBar.setProgress(progress);
        
        int percent = (stepGoal > 0) ? (int) ((double) steps / stepGoal * 100) : 0;
        stepProgressLabel.setText(String.format("%,d steps (%d%%)", steps, percent));

        // Load Workouts
        loadWorkouts();
        
        // Load Meals
        loadMeals();
        
        // Load Active Reminders
        List<Reminder> reminders = dbManager.getReminders(currentUser.getId());
        List<Reminder> activeReminders = new java.util.ArrayList<>();
        if (reminders != null) {
            for (Reminder r : reminders) {
                if (r.isActive()) {
                    activeReminders.add(r);
                }
            }
        }
        activeRemindersListView.setItems(FXCollections.observableArrayList(activeReminders));
        
        // Load Recent Activity
        loadRecentActivity();
    }

    private void loadRecentActivity() {
        if (currentUser == null) return;
        
        int recentSteps = dbManager.getTotalStepsLast7Days(currentUser.getId());
        recentStepsLabel.setText(String.valueOf(recentSteps));
        
        double recentDistance = recentSteps * 0.0008;
        recentDistanceLabel.setText(String.format("%.2f", recentDistance));
        
        int streak = dbManager.getCurrentWorkoutStreak(currentUser.getId());
        streakLabel.setText(streak + " Days");
        
        int recentCalories = dbManager.getCaloriesBurnedLast7Days(currentUser.getId());
        recentCaloriesLabel.setText(String.valueOf(recentCalories));
    }

    private void loadMeals() {
        if (currentUser == null) return;
        List<Meal> meals = dbManager.getMeals(currentUser.getId());
        mealTable.setItems(FXCollections.observableArrayList(meals));
    }

    private void loadWorkouts() {
        List<Workout> workouts = dbManager.getWorkouts(currentUser.getId());
        workoutTable.setItems(FXCollections.observableArrayList(workouts));
    }

    @FXML
    private void handleAddWorkout() {
        try {
            LocalDate date = workoutDate.getValue();
            String type = workoutType.getValue();
            if (type == null) type = "";
            int duration = Integer.parseInt(workoutDuration.getText());
            int calories = Integer.parseInt(workoutCalories.getText());
            String notes = workoutNotes.getText();

            if (date == null || type.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Date and Type are required.");
                return;
            }

            Workout workout = new Workout(currentUser.getId(), date, type, duration, calories, notes);
            if (dbManager.addWorkout(workout)) {
                loadDashboardData();
                clearWorkoutForm();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Workout added successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add workout.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Duration and Calories must be numbers.");
        }
    }

    private void clearWorkoutForm() {
        workoutType.setValue(null);
        workoutDuration.clear();
        workoutCalories.clear();
        workoutNotes.clear();
        workoutDate.setValue(LocalDate.now());
    }

    @FXML
    private void handleProfile() throws IOException {
        setActiveTab(btnYou);
        try {
            FXMLLoader loader = new FXMLLoader(FitTrackApp.class.getResource("/fxml/profile.fxml"));
            Parent profileView = loader.load();
            mainLayout.setCenter(profileView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleReminder() {
        setActiveTab(btnReminder);
        try {
            FXMLLoader loader = new FXMLLoader(FitTrackApp.class.getResource("/fxml/reminder.fxml"));
            Parent reminderView = loader.load();
            mainLayout.setCenter(reminderView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveTab(Button activeButton) {
        String defaultStyle = "-fx-background-color: transparent; -fx-font-size: 16px; -fx-text-fill: #757575;";
        String activeStyle = "-fx-background-color: transparent; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2196F3;";

        btnHome.setStyle(defaultStyle);
        btnReminder.setStyle(defaultStyle);
        btnYou.setStyle(defaultStyle);

        if (activeButton != null) {
            activeButton.setStyle(activeStyle);
        }
    }

    @FXML
    private void handleEditWorkout() {
        Workout selected = workoutTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedWorkoutForEdit = selected;
            workoutDate.setValue(selected.getDate());
            workoutType.setValue(selected.getType());
            workoutDuration.setText(String.valueOf(selected.getDurationMinutes()));
            workoutCalories.setText(String.valueOf(selected.getCaloriesBurned()));
            workoutNotes.setText(selected.getNotes());
            
            addWorkoutButton.setDisable(true);
            updateWorkoutButton.setDisable(false);
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a workout to edit.");
        }
    }

    @FXML
    private void handleUpdateWorkout() {
        if (selectedWorkoutForEdit == null) return;

        try {
            LocalDate date = workoutDate.getValue();
            String type = workoutType.getValue();
            if (type == null) type = "";
            int duration = Integer.parseInt(workoutDuration.getText());
            int calories = Integer.parseInt(workoutCalories.getText());
            String notes = workoutNotes.getText();

            if (date == null || type.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Date and Type are required.");
                return;
            }

            selectedWorkoutForEdit.setDate(date);
            selectedWorkoutForEdit.setType(type);
            selectedWorkoutForEdit.setDurationMinutes(duration);
            selectedWorkoutForEdit.setCaloriesBurned(calories);
            selectedWorkoutForEdit.setNotes(notes);

            if (dbManager.updateWorkout(selectedWorkoutForEdit)) {
                loadDashboardData();
                handleClearForm();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Workout updated successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update workout.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Duration and Calories must be numbers.");
        }
    }

    @FXML
    private void handleDeleteWorkout() {
        Workout selected = workoutTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this workout?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                if (dbManager.deleteWorkout(selected.getId())) {
                    loadDashboardData();
                    if (selectedWorkoutForEdit != null && selectedWorkoutForEdit.getId() == selected.getId()) {
                        handleClearForm();
                    }
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Workout deleted successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete workout.");
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a workout to delete.");
        }
    }

    @FXML
    private void handleClearForm() {
        clearWorkoutForm();
        selectedWorkoutForEdit = null;
        addWorkoutButton.setDisable(false);
        updateWorkoutButton.setDisable(true);
    }

    @FXML
    private void handleAddMeal() {
        try {
            LocalDate date = mealDate.getValue();
            String type = mealType.getValue();
            String food = foodItem.getText();
            int calories = Integer.parseInt(mealCalories.getText());

            if (date == null || type == null || food.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Date, Type, and Food Item are required.");
                return;
            }

            Meal meal = new Meal(currentUser.getId(), date, type, food, calories);
            if (dbManager.addMeal(meal)) {
                loadMeals();
                clearMealForm();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Meal added successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add meal.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Calories must be a number.");
        }
    }

    @FXML
    private void handleDeleteMeal() {
        Meal selected = mealTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this meal?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                if (dbManager.deleteMeal(selected.getId())) {
                    loadMeals();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Meal deleted successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete meal.");
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a meal to delete.");
        }
    }

    private void clearMealForm() {
        mealDate.setValue(LocalDate.now());
        mealType.getSelectionModel().clearSelection();
        foodItem.clear();
        mealCalories.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleHome() {
        setActiveTab(btnHome);
        mainLayout.setCenter(homeScrollPane);
        try {
            loadDashboardData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSetStepGoal() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(currentUser.getDailyStepGoal() > 0 ? currentUser.getDailyStepGoal() : 10000));
        dialog.setTitle("Set Daily Step Goal");
        dialog.setHeaderText("Enter your daily step goal:");
        dialog.setContentText("Steps:");

        dialog.showAndWait().ifPresent(result -> {
            try {
                int newGoal = Integer.parseInt(result);
                if (newGoal > 0) {
                    currentUser.setDailyStepGoal(newGoal);
                    if (dbManager.updateUser(currentUser)) {
                        loadDashboardData(); // Refresh UI
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to update goal.");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Goal must be positive.");
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number.");
            } catch (java.sql.SQLException e) {
                 e.printStackTrace();
                 showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update goal: " + e.getMessage());
            }
        });
    }
}
