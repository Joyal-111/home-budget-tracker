package com.mybudg;

import java.time.LocalDate;

public class Transaction {
    private int transactionId;
    private String username;
    private double amount;
    private String category;
    private String description;
    private String transactionType; // INCOME or EXPENSE
    private LocalDate transactionDate;

    public Transaction() {
    }

    public Transaction(String username, double amount, String category, String description, String transactionType, LocalDate transactionDate) {
        this.username = username;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.transactionType = transactionType;
        this.transactionDate = transactionDate;
    }

    // Getters and Setters
    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }
}
