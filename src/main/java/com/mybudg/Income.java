package com.mybudg;

import java.time.LocalDate;

public class Income {
    private int incomeId;
    private String username;
    private double amount;
    private String category;
    private LocalDate incomeDate;

    public Income() {
    }

    public Income(String username, double amount, String category, LocalDate incomeDate) {
        this.username = username;
        this.amount = amount;
        this.category = category;
        this.incomeDate = incomeDate;
    }

    // Getters and Setters
    public int getIncomeId() { return incomeId; }
    public void setIncomeId(int incomeId) { this.incomeId = incomeId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getIncomeDate() { return incomeDate; }
    public void setIncomeDate(LocalDate incomeDate) { this.incomeDate = incomeDate; }
}
