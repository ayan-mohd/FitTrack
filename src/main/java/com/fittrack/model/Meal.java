package com.fittrack.model;

import java.time.LocalDate;

public class Meal {
    private int id;
    private int userId;
    private LocalDate date;
    private String mealType;
    private String foodItem;
    private int calories;

    public Meal(int userId, LocalDate date, String mealType, String foodItem, int calories) {
        this.userId = userId;
        this.date = date;
        this.mealType = mealType;
        this.foodItem = foodItem;
        this.calories = calories;
    }

    public Meal() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public String getFoodItem() { return foodItem; }
    public void setFoodItem(String foodItem) { this.foodItem = foodItem; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }
}
