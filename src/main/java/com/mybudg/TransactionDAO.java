package com.mybudg;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TransactionDAO {

    public int saveTransaction(Transaction transaction) {
        String query = "INSERT INTO TRANSACTIONS (USERNAME, AMOUNT, CATEGORY, DESCRIPTION, TRANSACTION_TYPE, TRANSACTION_DATE) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, new String[]{"TRANSACTION_ID"})) {
            
            pstmt.setString(1, transaction.getUsername());
            pstmt.setDouble(2, transaction.getAmount());
            pstmt.setString(3, transaction.getCategory());
            pstmt.setString(4, transaction.getDescription());
            pstmt.setString(5, transaction.getTransactionType());
            pstmt.setDate(6, Date.valueOf(transaction.getTransactionDate()));
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                int generatedId = 0;
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                    }
                }

                // Also save to separate table
                String secondaryQuery;
                if ("INCOME".equalsIgnoreCase(transaction.getTransactionType())) {
                    secondaryQuery = "INSERT INTO INCOME (USERNAME, AMOUNT, CATEGORY, INCOME_DATE) VALUES (?, ?, ?, ?)";
                } else {
                    secondaryQuery = "INSERT INTO EXPENSE (USERNAME, AMOUNT, CATEGORY, EXPENSE_DATE) VALUES (?, ?, ?, ?)";
                }

                try (PreparedStatement secPstmt = conn.prepareStatement(secondaryQuery)) {
                    secPstmt.setString(1, transaction.getUsername());
                    secPstmt.setDouble(2, transaction.getAmount());
                    secPstmt.setString(3, transaction.getCategory());
                    secPstmt.setDate(4, Date.valueOf(transaction.getTransactionDate()));
                    secPstmt.executeUpdate();
                }

                return generatedId;
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Save transaction error: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    public double getTotalAmountByType(String username, String type) {
        String query = "SELECT SUM(AMOUNT) FROM TRANSACTIONS WHERE USERNAME = ? AND TRANSACTION_TYPE = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, type);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Get total amount error: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    public Map<String, Double> getCategoryWiseTotals(String username, String type) {
        Map<String, Double> totals = new HashMap<>();
        String query = "SELECT CATEGORY, SUM(AMOUNT) FROM TRANSACTIONS WHERE USERNAME = ? AND TRANSACTION_TYPE = ? GROUP BY CATEGORY";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, type);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    totals.put(rs.getString("CATEGORY"), rs.getDouble(2));
                }
            }
        } catch (SQLException e) {
            System.err.println("Get category wise totals error: " + e.getMessage());
            e.printStackTrace();
        }
        return totals;
    }

    public List<Transaction> getAllTransactions(String username) {
        List<Transaction> list = new ArrayList<>();
        String query = "SELECT * FROM TRANSACTIONS WHERE USERNAME = ? ORDER BY TRANSACTION_DATE DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transaction t = new Transaction();
                    t.setTransactionId(rs.getInt("TRANSACTION_ID"));
                    t.setUsername(rs.getString("USERNAME"));
                    t.setAmount(rs.getDouble("AMOUNT"));
                    t.setCategory(rs.getString("CATEGORY"));
                    t.setDescription(rs.getString("DESCRIPTION"));
                    t.setTransactionType(rs.getString("TRANSACTION_TYPE"));
                    t.setTransactionDate(rs.getDate("TRANSACTION_DATE").toLocalDate());
                    list.add(t);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean deleteTransaction(int transactionId) {
        String query = "DELETE FROM TRANSACTIONS WHERE TRANSACTION_ID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, transactionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Double> getMonthlyTrends(String username, String type) {
        Map<String, Double> trends = new LinkedHashMap<>();
        String query = "SELECT TO_CHAR(TRANSACTION_DATE, 'YYYY-MM') AS MONTH, SUM(AMOUNT) FROM TRANSACTIONS WHERE USERNAME = ? AND TRANSACTION_TYPE = ? GROUP BY TO_CHAR(TRANSACTION_DATE, 'YYYY-MM') ORDER BY MONTH";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, type);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    trends.put(rs.getString(1), rs.getDouble(2));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trends;
    }

    public boolean updateTransaction(Transaction transaction) {
        String query = "UPDATE TRANSACTIONS SET AMOUNT=?, CATEGORY=?, DESCRIPTION=?, TRANSACTION_TYPE=?, TRANSACTION_DATE=? WHERE TRANSACTION_ID=? AND USERNAME=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDouble(1, transaction.getAmount());
            pstmt.setString(2, transaction.getCategory());
            pstmt.setString(3, transaction.getDescription());
            pstmt.setString(4, transaction.getTransactionType());
            pstmt.setDate(5, Date.valueOf(transaction.getTransactionDate()));
            pstmt.setInt(6, transaction.getTransactionId());
            pstmt.setString(7, transaction.getUsername());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
