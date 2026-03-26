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

public class SignupController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    private TextField emailField;

    private final UserDAO userDAO = new UserDAO();
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML private javafx.scene.layout.HBox titleBar;

    public void initialize() {
        // Dragging logic
        if (titleBar != null) {
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
        }
    }

    @FXML
    private void closeWindow(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void minimizeWindow(ActionEvent event) {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void handleSignup(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Please fill in all fields.");
            return;
        }

        javafx.concurrent.Task<Integer> signupTask = new javafx.concurrent.Task<>() {
            @Override
            protected Integer call() throws Exception {
                return userDAO.register(username, password, email);
            }
        };

        signupTask.setOnSucceeded(e -> {
            int userId = signupTask.getValue();
            if (userId > 0) {
                UserSession.initialize(userId, username, email);
                showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "User registered successfully!");
                try {
                    switchToMain();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else if (userId == -1) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Connection failed. Please check DatabaseConfig.java.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Registration Failed", "Username or Email might already exist.");
            }
        });

        signupTask.setOnFailed(e -> {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error: " + signupTask.getException().getMessage());
        });

        new Thread(signupTask).start();
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

    @FXML
    private void switchToLogin() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        usernameField.getScene().setRoot(root);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
