package com.mybudg;

public class UserSession {
    private static UserSession instance;
    private int userId;
    private String username;
    private String email;

    private UserSession(int userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    public static void initialize(int userId, String username, String email) {
        instance = new UserSession(userId, username, email);
    }

    public static UserSession getInstance() {
        return instance;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public static void cleanUserSession() {
        instance = null;
    }
}
