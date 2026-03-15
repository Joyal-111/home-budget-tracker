package com.mybudg;

import java.time.LocalDate;

public class Expense {
    private int expenseId;
    private String username;
    private double amount;
    private String category;
    private LocalDate expenseDate;

    public Expense() {
    }

    public Expense(String username, double amount, String category, LocalDate expenseDate) {
        this.username = username;
        this.amount = amount;
        this.category = category;
        this.expenseDate = expenseDate;
    }

    // Getters and Setters
    public int getExpenseId() { return expenseId; }
    public void setExpenseId(int expenseId) { this.expenseId = expenseId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
}
