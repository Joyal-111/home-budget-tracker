package com.mybudg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BudgetDAO {

    public double getBudgetAmount(String username, String category) {
        String query = "SELECT AMOUNT FROM BUDGET WHERE USERNAME = ? AND CATEGORY = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, category);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("AMOUNT");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public double getCurrentSpending(String username, String category, String monthYear) {
        // monthYear format: 'YYYY-MM'
        String query = "SELECT SUM(AMOUNT) FROM TRANSACTIONS WHERE USERNAME = ? AND CATEGORY = ? AND TRANSACTION_TYPE = 'EXPENSE' AND TO_CHAR(TRANSACTION_DATE, 'YYYY-MM') = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, category);
            pstmt.setString(3, monthYear);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
