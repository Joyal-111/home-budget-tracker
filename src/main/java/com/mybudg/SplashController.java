package com.mybudg;

import java.io.IOException;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashController {

    @FXML
    private javafx.scene.layout.HBox titleBar;

    @FXML
    private VBox logoContainer;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label statusLabel;

    private double xOffset = 0;
    private double yOffset = 0;

    public void initialize() {
        startAnimations();
        
        // Handle dragging via title bar
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        // Also keep logo dragging for better UX
        logoContainer.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        logoContainer.setOnMouseDragged(event -> {
            Stage stage = (Stage) logoContainer.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String[] statusMessages = {
                    "Loading configuration...",
                    "Checking wallet balance...",
                    "Analyzing spending patterns...",
                    "Syncing transactions...",
                    "Ready to budget!"
                };

                for (int i = 0; i < statusMessages.length; i++) {
                    final int index = i;
                    Platform.runLater(() -> statusLabel.setText(statusMessages[index]));
                    
                    for (int j = 0; j < 20; j++) {
                        updateProgress((index * 20) + j + 1, 100);
                        Thread.sleep(30); // Faster for testing
                    }
                }
                return null;
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());

        task.setOnSucceeded(event -> switchToLogin());

        new Thread(task).start();
    }

    private void startAnimations() {
        ScaleTransition scale = new ScaleTransition(Duration.seconds(2), logoContainer);
        scale.setFromX(0.5);
        scale.setFromY(0.5);
        scale.setToX(1.0);
        scale.setToY(1.0);

        FadeTransition fade = new FadeTransition(Duration.seconds(2), logoContainer);
        fade.setFromValue(0);
        fade.setToValue(1);

        ParallelTransition pt = new ParallelTransition(scale, fade);
        pt.play();
    }

    @FXML
    private void closeWindow(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void minimizeWindow(ActionEvent event) {
        Stage stage = (Stage) progressBar.getScene().getWindow();
        stage.setIconified(true);
    }

    private void switchToLogin() {
        try {
            System.out.println("Switching to login page...");
            Stage stage = (Stage) progressBar.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/mybudg/login.fxml"));
            stage.getScene().setRoot(root);
            System.out.println("Transitioned to login page.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
