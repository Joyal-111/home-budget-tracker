package com.mybudg;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private javafx.scene.layout.HBox titleBar;
    @FXML private javafx.scene.control.Button minimizeBtn;
    @FXML private javafx.scene.control.Button maximizeBtn;
    @FXML private javafx.scene.control.Button closeBtn;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private final UserDAO userDAO = new UserDAO();
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isMaximized = false;
    private double lastWidth = 450;
    private double lastHeight = 550;
    private double lastX = 0;
    private double lastY = 0;

    private boolean isDragging = false;

    public void initialize() {
        System.out.println("Login Page: Initialized.");

        // Dragging logic
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            if (stage != null && !stage.isMaximized()) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });

        // Double click to maximize
        titleBar.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) toggleMaximize();
        });

        // PREVENT dragging when interacting with window controls
        minimizeBtn.setOnMousePressed(event -> event.consume());
        maximizeBtn.setOnMousePressed(event -> event.consume());
        closeBtn.setOnMousePressed(event -> event.consume());
    }

    @FXML
    private void toggleMaximize() {
        System.out.println("Login: Maximize Toggled");
        Stage stage = (Stage) titleBar.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
    }

    @FXML
    private void closeWindow(ActionEvent event) {
        System.out.println("Login: Close Clicked");
        javafx.application.Platform.exit();
        System.exit(0);
    }

    @FXML
    private void minimizeWindow(ActionEvent event) {
        System.out.println("Login: Minimize Clicked");
        Stage stage = (Stage) titleBar.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Login", "Please enter username and password.");
            return;
        }

        // Run login in a background task to keep UI responsive
        javafx.concurrent.Task<User> loginTask = new javafx.concurrent.Task<>() {
            @Override
            protected User call() throws Exception {
                return userDAO.login(username, password);
            }
        };

        loginTask.setOnSucceeded(e -> {
            User user = loginTask.getValue();
            if (user != null) {
                UserSession.initialize(user.getUserId(), user.getUsername(), user.getEmail());
                try {
                    switchToMain();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
            }
        });

        loginTask.setOnFailed(e -> {
            Throwable ex = loginTask.getException();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not connect to database: " + ex.getMessage());
        });

        new Thread(loginTask).start();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleSignUp() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("signup.fxml"));
        usernameField.getScene().setRoot(root);
    }

    private void switchToMain() throws IOException {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        stage.getScene().setRoot(root);
        if (!stage.isMaximized()) {
            stage.setWidth(1000);
            stage.setHeight(700);
            stage.centerOnScreen();
        }
    }
}
