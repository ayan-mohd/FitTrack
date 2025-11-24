package com.fittrack.model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private int age;
    private float weight;
    private float height;
    private String sex;
    private String role;
    private LocalDateTime createdAt;
    
    // New Goal Fields
    private int dailyStepGoal;
    private int weeklyWorkoutGoal;
    private float weightTarget;

    public User() {}

    public User(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = "USER";
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public float getWeight() { return weight; }
    public void setWeight(float weight) { this.weight = weight; }

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // Goal Getters and Setters
    public int getDailyStepGoal() { return dailyStepGoal; }
    public void setDailyStepGoal(int dailyStepGoal) { this.dailyStepGoal = dailyStepGoal; }

    public int getWeeklyWorkoutGoal() { return weeklyWorkoutGoal; }
    public void setWeeklyWorkoutGoal(int weeklyWorkoutGoal) { this.weeklyWorkoutGoal = weeklyWorkoutGoal; }

    public float getWeightTarget() { return weightTarget; }
    public void setWeightTarget(float weightTarget) { this.weightTarget = weightTarget; }
}
