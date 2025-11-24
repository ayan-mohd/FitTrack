package com.fittrack.model;

import java.time.LocalTime;

public class Reminder {
    private int id;
    private int userId;
    private String workoutType;
    private String day;
    private LocalTime time;
    private boolean isActive;

    public Reminder() {}

    public Reminder(int userId, String workoutType, String day, LocalTime time, boolean isActive) {
        this.userId = userId;
        this.workoutType = workoutType;
        this.day = day;
        this.time = time;
        this.isActive = isActive;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getWorkoutType() { return workoutType; }
    public void setWorkoutType(String workoutType) { this.workoutType = workoutType; }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
