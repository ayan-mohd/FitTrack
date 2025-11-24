package com.fittrack.db;

import com.fittrack.model.Meal;
import com.fittrack.model.Reminder;
import com.fittrack.model.User;
import com.fittrack.model.Workout;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DatabaseManager {
    private static String dbUrl;
    private static String dbUser;
    private static String dbPass;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            loadProperties();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void loadProperties() {
        try (InputStream input = DatabaseManager.class.getClassLoader().getResourceAsStream("db.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                System.out.println("Sorry, unable to find db.properties");
                return;
            }
            prop.load(input);
            dbUrl = prop.getProperty("db.url");
            dbUser = prop.getProperty("db.user");
            dbPass = prop.getProperty("db.password");
            System.out.println("Database Config Loaded:");
            System.out.println("URL: " + dbUrl);
            System.out.println("User: " + dbUser);
            System.out.println("Password Length: " + (dbPass != null ? dbPass.length() : "null"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPass);
    }

    public boolean validateLogin(String email, String passwordHash) {
        String sql = "SELECT * FROM users WHERE email = ? AND password_hash = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, passwordHash);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean registerUser(User user) throws SQLException {
        String sql = "INSERT INTO users (name, email, password_hash, age, weight, height, sex) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPasswordHash());
            pstmt.setInt(4, user.getAge());
            pstmt.setFloat(5, user.getWeight());
            pstmt.setFloat(6, user.getHeight());
            pstmt.setString(7, user.getSex());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public void initializeDatabase() {
        // Create database if it doesn't exist
        createDatabaseIfNotExists();

        // In a real app, better to load from resources
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
             
             checkAndDropLegacySchema(conn);

             // For simplicity, let's define the schema creation directly or read from file
             // Here we will execute the CREATE TABLE statements directly for robustness in this demo
             String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "name VARCHAR(50) NOT NULL, " +
                     "email VARCHAR(100) UNIQUE NOT NULL, " +
                     "password_hash VARCHAR(255) NOT NULL, " +
                     "age INT, " +
                     "weight FLOAT, " +
                     "height FLOAT, " +
                     "sex VARCHAR(10), " +
                     "role VARCHAR(20) DEFAULT 'USER', " +
                     "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
                     
             String createWorkouts = "CREATE TABLE IF NOT EXISTS workouts (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "user_id INT NOT NULL, " +
                     "date DATE NOT NULL, " +
                     "type VARCHAR(50), " +
                     "duration_minutes INT, " +
                     "calories_burned INT, " +
                     "notes TEXT, " +
                     "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)";
                     
             String createSteps = "CREATE TABLE IF NOT EXISTS steps (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "user_id INT NOT NULL, " +
                     "date DATE NOT NULL, " +
                     "steps INT DEFAULT 0, " +
                     "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)";

             String createMeals = "CREATE TABLE IF NOT EXISTS meals (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "user_id INT NOT NULL, " +
                     "date DATE NOT NULL, " +
                     "meal_type VARCHAR(50), " +
                     "food_item VARCHAR(100), " +
                     "calories INT, " +
                     "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)";

             String createReminders = "CREATE TABLE IF NOT EXISTS reminders (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "user_id INT NOT NULL, " +
                     "workout_type VARCHAR(50), " +
                     "day_of_week VARCHAR(20), " +
                     "time TIME NOT NULL, " +
                     "is_active BOOLEAN DEFAULT TRUE, " +
                     "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                     ")";

             stmt.execute(createUsers);
             stmt.execute(createWorkouts);
             stmt.execute(createSteps);
             stmt.execute(createMeals);
             stmt.execute(createReminders);
             
             try {
                 stmt.execute("ALTER TABLE reminders ADD COLUMN day_of_week VARCHAR(20)");
             } catch (SQLException e) {
                 // Column likely already exists or table created with it
             }
             
             try {
                 stmt.execute("ALTER TABLE users ADD COLUMN daily_step_goal INT DEFAULT 0");
                 stmt.execute("ALTER TABLE users ADD COLUMN weekly_workout_goal INT DEFAULT 0");
                 stmt.execute("ALTER TABLE users ADD COLUMN weight_target FLOAT DEFAULT 0.0");
             } catch (SQLException e) {
                 // Columns likely already exist
             }

             System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createDatabaseIfNotExists() {
        String dbName = "fittrack";
        // Construct URL to connect to server only (no database selected)
        // Assumes dbUrl format: jdbc:mysql://host:port/dbname?params
        String serverUrl = dbUrl.replace("/" + dbName, "");
        
        try (Connection conn = DriverManager.getConnection(serverUrl, dbUser, dbPass);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
            System.out.println("Database '" + dbName + "' checked/created.");
        } catch (SQLException e) {
            System.err.println("Warning: Could not create database (might already exist or permission denied): " + e.getMessage());
            // We don't throw here, we let the main connection attempt fail if the DB is truly missing/inaccessible
        }
    }

    private void checkAndDropLegacySchema(Connection conn) throws SQLException {
        DatabaseMetaData md = conn.getMetaData();
        try (ResultSet rs = md.getTables(null, null, "users", null)) {
            if (rs.next()) {
                // Table exists, check for required columns
                boolean schemaChanged = false;
                
                // Check if 'name' column is missing (new schema)
                try (ResultSet cols = md.getColumns(null, null, "users", "name")) {
                    if (!cols.next()) schemaChanged = true;
                }
                
                // Check if 'username' column exists (old schema)
                if (!schemaChanged) {
                    try (ResultSet cols = md.getColumns(null, null, "users", "username")) {
                        if (cols.next()) schemaChanged = true;
                    }
                }

                if (schemaChanged) {
                    System.out.println("Detected legacy 'users' table schema. Dropping tables to recreate...");
                    try (Statement stmt = conn.createStatement()) {
                        // Drop dependent tables first
                        stmt.execute("DROP TABLE IF EXISTS meals");
                        stmt.execute("DROP TABLE IF EXISTS steps");
                        stmt.execute("DROP TABLE IF EXISTS workouts");
                        stmt.execute("DROP TABLE IF EXISTS users");
                    }
                }
            }
        }
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setAge(rs.getInt("age"));
                    user.setWeight(rs.getFloat("weight"));
                    user.setHeight(rs.getFloat("height"));
                    user.setSex(rs.getString("sex"));
                    user.setRole(rs.getString("role"));
                    user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    
                    // Load Goals
                    try {
                        user.setDailyStepGoal(rs.getInt("daily_step_goal"));
                        user.setWeeklyWorkoutGoal(rs.getInt("weekly_workout_goal"));
                        user.setWeightTarget(rs.getFloat("weight_target"));
                    } catch (SQLException e) {
                        // Columns might not exist yet if migration failed or old DB
                        System.err.println("Warning: Could not load user goals: " + e.getMessage());
                    }
                    
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addWorkout(Workout workout) {
        String sql = "INSERT INTO workouts (user_id, date, type, duration_minutes, calories_burned, notes) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, workout.getUserId());
            pstmt.setDate(2, Date.valueOf(workout.getDate()));
            pstmt.setString(3, workout.getType());
            pstmt.setInt(4, workout.getDurationMinutes());
            pstmt.setInt(5, workout.getCaloriesBurned());
            pstmt.setString(6, workout.getNotes());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Workout> getWorkouts(int userId) {
        List<Workout> workouts = new ArrayList<>();
        String sql = "SELECT * FROM workouts WHERE user_id = ? ORDER BY date DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Workout workout = new Workout();
                    workout.setId(rs.getInt("id"));
                    workout.setUserId(rs.getInt("user_id"));
                    workout.setDate(rs.getDate("date").toLocalDate());
                    workout.setType(rs.getString("type"));
                    workout.setDurationMinutes(rs.getInt("duration_minutes"));
                    workout.setCaloriesBurned(rs.getInt("calories_burned"));
                    workout.setNotes(rs.getString("notes"));
                    workouts.add(workout);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return workouts;
    }

    public boolean updateSteps(int userId, int steps, LocalDate date) {
        // Check if entry exists for today
        String checkSql = "SELECT id FROM steps WHERE user_id = ? AND date = ?";
        String updateSql = "UPDATE steps SET steps = ? WHERE id = ?";
        String insertSql = "INSERT INTO steps (user_id, date, steps) VALUES (?, ?, ?)";

        try (Connection conn = getConnection()) {
            int existingId = -1;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, userId);
                checkStmt.setDate(2, Date.valueOf(date));
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        existingId = rs.getInt("id");
                    }
                }
            }

            if (existingId != -1) {
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, steps);
                    updateStmt.setInt(2, existingId);
                    return updateStmt.executeUpdate() > 0;
                }
            } else {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setDate(2, Date.valueOf(date));
                    insertStmt.setInt(3, steps);
                    return insertStmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getSteps(int userId, LocalDate date) {
        String sql = "SELECT steps FROM steps WHERE user_id = ? AND date = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(date));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("steps");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET name = ?, age = ?, weight = ?, height = ?, daily_step_goal = ?, weekly_workout_goal = ?, weight_target = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getName());
            pstmt.setInt(2, user.getAge());
            pstmt.setFloat(3, user.getWeight());
            pstmt.setFloat(4, user.getHeight());
            pstmt.setInt(5, user.getDailyStepGoal());
            pstmt.setInt(6, user.getWeeklyWorkoutGoal());
            pstmt.setFloat(7, user.getWeightTarget());
            pstmt.setInt(8, user.getId());
            
            return pstmt.executeUpdate() > 0;
        }
    }

    public int getWorkoutCountLast7Days(int userId) {
        String sql = "SELECT COUNT(*) FROM workouts WHERE user_id = ? AND date >= ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(LocalDate.now().minusDays(7)));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalCaloriesBurned(int userId) {
        String sql = "SELECT SUM(calories_burned) FROM workouts WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean updateWorkout(Workout workout) {
        String sql = "UPDATE workouts SET date = ?, type = ?, duration_minutes = ?, calories_burned = ?, notes = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(workout.getDate()));
            pstmt.setString(2, workout.getType());
            pstmt.setInt(3, workout.getDurationMinutes());
            pstmt.setInt(4, workout.getCaloriesBurned());
            pstmt.setString(5, workout.getNotes());
            pstmt.setInt(6, workout.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteWorkout(int workoutId) {
        String sql = "DELETE FROM workouts WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, workoutId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addMeal(Meal meal) {
        String sql = "INSERT INTO meals (user_id, date, meal_type, food_item, calories) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, meal.getUserId());
            pstmt.setDate(2, Date.valueOf(meal.getDate()));
            pstmt.setString(3, meal.getMealType());
            pstmt.setString(4, meal.getFoodItem());
            pstmt.setInt(5, meal.getCalories());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Meal> getMeals(int userId) {
        List<Meal> meals = new ArrayList<>();
        String sql = "SELECT * FROM meals WHERE user_id = ? ORDER BY date DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Meal meal = new Meal();
                    meal.setId(rs.getInt("id"));
                    meal.setUserId(rs.getInt("user_id"));
                    meal.setDate(rs.getDate("date").toLocalDate());
                    meal.setMealType(rs.getString("meal_type"));
                    meal.setFoodItem(rs.getString("food_item"));
                    meal.setCalories(rs.getInt("calories"));
                    meals.add(meal);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return meals;
    }

    public boolean deleteMeal(int mealId) {
        String sql = "DELETE FROM meals WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mealId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getTotalStepsLast7Days(int userId) {
        String sql = "SELECT SUM(steps) FROM steps WHERE user_id = ? AND date >= ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(LocalDate.now().minusDays(7)));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getCurrentWorkoutStreak(int userId) {
        String sql = "SELECT DISTINCT date FROM workouts WHERE user_id = ? ORDER BY date DESC";
        int streak = 0;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                LocalDate lastDate = null;
                LocalDate today = LocalDate.now();
                
                while (rs.next()) {
                    LocalDate workoutDate = rs.getDate("date").toLocalDate();
                    
                    if (lastDate == null) {
                        if (workoutDate.equals(today) || workoutDate.equals(today.minusDays(1))) {
                            streak++;
                            lastDate = workoutDate;
                        } else {
                            // Streak broken or hasn't started recently
                            break;
                        }
                    } else {
                        if (workoutDate.equals(lastDate.minusDays(1))) {
                            streak++;
                            lastDate = workoutDate;
                        } else {
                            break;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return streak;
    }

    public int getCaloriesBurnedLast7Days(int userId) {
        String sql = "SELECT SUM(calories_burned) FROM workouts WHERE user_id = ? AND date >= ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(LocalDate.now().minusDays(7)));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getWorkoutCountToday(int userId) {
        String sql = "SELECT COUNT(*) FROM workouts WHERE user_id = ? AND date = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(LocalDate.now()));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getCaloriesBurnedToday(int userId) {
        String sql = "SELECT SUM(calories_burned) FROM workouts WHERE user_id = ? AND date = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(LocalDate.now()));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalSteps(int userId) {
        String sql = "SELECT SUM(steps) FROM steps WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalWorkoutDays(int userId) {
        String sql = "SELECT COUNT(DISTINCT date) FROM workouts WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean addReminder(Reminder reminder) {
        String sql = "INSERT INTO reminders (user_id, workout_type, day_of_week, time, is_active) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reminder.getUserId());
            pstmt.setString(2, reminder.getWorkoutType());
            pstmt.setString(3, reminder.getDay());
            pstmt.setTime(4, Time.valueOf(reminder.getTime()));
            pstmt.setBoolean(5, reminder.isActive());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Reminder> getReminders(int userId) {
        List<Reminder> reminders = new ArrayList<>();
        String sql = "SELECT * FROM reminders WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Reminder reminder = new Reminder();
                    reminder.setId(rs.getInt("id"));
                    reminder.setUserId(rs.getInt("user_id"));
                    reminder.setWorkoutType(rs.getString("workout_type"));
                    reminder.setDay(rs.getString("day_of_week"));
                    reminder.setTime(rs.getTime("time").toLocalTime());
                    reminder.setActive(rs.getBoolean("is_active"));
                    reminders.add(reminder);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reminders;
    }

    public boolean deleteReminder(int id) {
        String sql = "DELETE FROM reminders WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateReminderStatus(int id, boolean isActive) {
        String sql = "UPDATE reminders SET is_active = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, isActive);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
