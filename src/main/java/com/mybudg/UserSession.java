package com.mybudg;

public class UserSession {
    private static UserSession instance;
    private int userId;
    private String username;

    private UserSession(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public static void initialize(int userId, String username) {
        instance = new UserSession(userId, username);
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

    public static void cleanUserSession() {
        instance = null;
    }
}
