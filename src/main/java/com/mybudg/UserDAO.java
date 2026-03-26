package com.mybudg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public User login(String identifier, String password) {
        String query = "SELECT USER_ID, USERNAME, EMAIL FROM USERS WHERE (USERNAME = ? OR EMAIL = ?) AND PASSWORD = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, identifier);
            pstmt.setString(2, identifier);
            pstmt.setString(3, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("USER_ID"), rs.getString("USERNAME"), rs.getString("EMAIL"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public int register(String username, String password, String email) {
        String query = "INSERT INTO USERS (USERNAME, PASSWORD, EMAIL) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, new String[]{"USER_ID"})) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            if (e.getErrorCode() == 1017 || e.getErrorCode() == 17002) {
                return -1; // Connection error
            }
            return 0; // Likely constraint violation (already exists)
        }
    }
    public boolean updatePassword(int userId, String newPassword) {
        String query = "UPDATE USERS SET PASSWORD = ? WHERE USER_ID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getEmail(int userId) {
        String query = "SELECT EMAIL FROM USERS WHERE USER_ID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getString("EMAIL");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
