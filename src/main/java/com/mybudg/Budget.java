package com.mybudg;

public class Budget {
    private int budgetId;
    private String username;
    private String category;
    private double amount;
    private String period; // e.g., MONTHLY

    public Budget() {
    }

    public Budget(String username, String category, double amount, String period) {
        this.username = username;
        this.category = category;
        this.amount = amount;
        this.period = period;
    }

    // Getters and Setters
    public int getBudgetId() { return budgetId; }
    public void setBudgetId(int budgetId) { this.budgetId = budgetId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
}
