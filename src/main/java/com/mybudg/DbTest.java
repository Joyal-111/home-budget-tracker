package com.mybudg;

import java.sql.Connection;
import java.sql.SQLException;

public class DbTest {
    public static void main(String[] args) {
        System.out.println("Testing connection via DatabaseConfig...");
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            System.out.println("SUCCESS: Connection established via DatabaseConfig!");
            System.out.println("Connected user: " + conn.getMetaData().getUserName());
        } catch (SQLException e) {
            System.err.println("FAILURE: SQL error occurred.");
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
