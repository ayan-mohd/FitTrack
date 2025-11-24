# FitTrack - Your Personal Wellness Companion

FitTrack is a comprehensive, modern desktop application designed to help you monitor your fitness journey, track your nutrition, and stay consistent with your wellness goals. Built with JavaFX, it features a sleek **Dark Mode** interface with intuitive navigation and powerful tracking capabilities.

## 🌟 Key Features

### 1. **Interactive Dashboard**
The central hub of your fitness journey.
- **Daily Summary**: Instantly view today's total workouts and calories burned.
- **Step Tracker**: Set a daily step goal and track your progress with a visual progress bar.
- **Recent Activity**: Quick stats on your recent steps, distance covered, and activity streak.
- **Active Reminders**: See your upcoming workout reminders at a glance.

### 2. **Workout Management**
- **Log Workouts**: Record details like Date, Type (Running, Gym, Yoga, etc.), Duration, Calories, and Notes.
- **Visual Coding**: Workout types are color-coded with pastel accents for easy recognition against the dark theme.
- **Edit & Delete**: Easily modify or remove entries if you made a mistake.

### 3. **Nutrition Tracking**
- **Meal Logging**: Keep track of your daily meals (Breakfast, Lunch, Dinner, Snack).
- **Calorie Counting**: Monitor your caloric intake to maintain a balanced diet.

### 4. **Smart Reminders**
- **Custom Schedules**: Set reminders for specific days and times.
- **Toggle Active Status**: Easily switch reminders ON or OFF without deleting them.
- **Visual Indicators**: Reminders are color-coded by workout type.

### 5. **Profile & Health Metrics**
- **BMI Calculator**: Input your height and weight to instantly calculate your Body Mass Index.
- **Body Metrics**: Update your age, weight, and height to keep your profile current.
- **Total Activity Stats**: View your all-time stats including Total Steps, Total Calories Burned, and Total Active Days.

### 6. **Modern UI/UX**
- **Dark Theme**: A custom-designed dark interface (`#2e3135`) that reduces eye strain.
- **Responsive Navigation**: Seamlessly switch between Home, Reminders, and Profile tabs with active state highlighting.
- **Split-Screen Login**: A stylish login experience with a dedicated logo area.

---

## 🚀 Getting Started

### Prerequisites
- **Java JDK 17** or higher.
- **Maven** (for building the project).
- **MySQL Database**.

### Installation & Setup

1.  **Clone the Repository**
    ```bash
    git clone https://github.com/yourusername/FitTrack.git
    cd FitTrack
    ```

2.  **Database Setup**
    - Create a MySQL database named `fittrack`.
    - Import the schema from `schema.sql` to create the necessary tables.
    ```sql
    CREATE DATABASE fittrack;
    USE fittrack;
    -- Run contents of schema.sql
    ```

3.  **Configure Database Connection**
    - Open `src/main/resources/db.properties`.
    - Update the `db.user` and `db.password` fields with your MySQL credentials.
    ```properties
    db.url=jdbc:mysql://localhost:3306/fittrack?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    db.user=YOUR_USERNAME
    db.password=YOUR_PASSWORD
    ```

4.  **Build the Application**
    ```bash
    mvn clean package
    ```

5.  **Run the Application**
    ```bash
    mvn javafx:run
    ```

---

## 📖 Usage Guide

### **Login & Registration**
- Launch the app.
- Click **"Register here"** to create a new account.
- Log in with your username and password.

### **Managing Workouts**
1.  Navigate to the **Home** tab.
2.  On the left panel, select a Date, Workout Type, Duration, and Calories.
3.  Click **"Add Workout"**.
4.  To edit, select a workout from the table, modify the fields, and click **"Update"**.
5.  To delete, select a workout and click **"Delete Selected"**.

### **Setting Reminders**
1.  Click the **"Reminder"** button in the bottom navigation bar.
2.  Select a Day and Workout Type (or type a custom one).
3.  Enter the time (e.g., "07:00").
4.  Click **"Set Reminder"**.
5.  Use the **ON/OFF** toggle button in the list to enable or disable specific reminders.

### **Updating Profile**
1.  Click the **"You"** button in the bottom navigation bar.
2.  Update your Age, Weight, or Height.
3.  Click **"Calculate"** next to BMI to see your current health index.
4.  Click **"Save Changes"** to persist your data.

---

## 🛠 Technologies Used
- **JavaFX**: For the rich desktop user interface.
- **Java 20**: Core programming language.
- **MySQL**: Relational database for data persistence.
- **Maven**: Dependency management and build tool.
- **CSS**: Custom styling for the Dark Mode theme.

---

## 🤝 Contributing
Contributions are welcome! Please fork the repository and submit a pull request for any enhancements or bug fixes.

---

**FitTrack** - *Your wellness journey is growing every day.*