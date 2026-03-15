package com.mybudg;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class SettingsController implements Initializable {

    @FXML private HBox titleBar;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    private UserDAO userDAO = new UserDAO();
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isMaximized = false;
    private double lastWidth = 600;
    private double lastHeight = 450;
    private double lastX = 0;
    private double lastY = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            if (stage.isMaximized()) return;
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        titleBar.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) toggleMaximize();
        });
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Fields cannot be empty.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match.");
            return;
        }

        int userId = UserSession.getInstance().getUserId();
        if (userDAO.updatePassword(userId, newPass)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated successfully!");
            newPasswordField.clear();
            confirmPasswordField.clear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update password.");
        }
    }

    private void navigateTo(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            titleBar.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewDashboard() {
        navigateTo("main.fxml");
    }

    @FXML
    private void handleAddTransaction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("add-transaction.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewTransactions() {
        navigateTo("transactions.fxml");
    }

    @FXML
    private void handleViewReports() {
        navigateTo("analysis.fxml");
    }

    @FXML
    private void handleSettings() {
        navigateTo("settings.fxml");
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        handleViewDashboard();
    }

    @FXML
    private void toggleMaximize() {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        if (isMaximized) {
            stage.setWidth(lastWidth);
            stage.setHeight(lastHeight);
            stage.setX(lastX);
            stage.setY(lastY);
            isMaximized = false;
        } else {
            lastWidth = stage.getWidth();
            lastHeight = stage.getHeight();
            lastX = stage.getX();
            lastY = stage.getY();
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());
            isMaximized = true;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }

    @FXML
    private void closeWindow(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void minimizeWindow(ActionEvent event) {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        stage.setIconified(true);
    }
}
