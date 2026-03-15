package com.mybudg;

import java.io.IOException;
import java.util.Map;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AnalysisController {

    @FXML private HBox titleBar;
    @FXML private PieChart expenseChart;
    @FXML private PieChart incomeChart;
    @FXML private javafx.scene.layout.VBox card1;
    @FXML private javafx.scene.layout.VBox card2;

    private TransactionDAO transactionDAO = new TransactionDAO();
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isMaximized = false;
    private double lastWidth = 950;
    private double lastHeight = 700;
    private double lastX = 0;
    private double lastY = 0;

    @FXML
    public void initialize() {
        // Handle window dragging
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

        // Minimalistic Animations
        applyFadeIn(card1, 200);
        applyFadeIn(card2, 400);

        // Delay slightly for cool entrance effect
        PauseTransition delay = new PauseTransition(Duration.millis(500));
        delay.setOnFinished(e -> loadChartData());
        delay.play();
    }

    private void applyFadeIn(javafx.scene.Node node, int delayMs) {
        if (node == null) return;
        node.setOpacity(0);
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(Duration.millis(800), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setDelay(Duration.millis(delayMs));
        ft.play();
    }

    private void loadChartData() {
        // Clear previous data for animation
        expenseChart.getData().clear();
        incomeChart.getData().clear();

        // Ensure animations are on
        expenseChart.setAnimated(true);
        incomeChart.setAnimated(true);

        String username = UserSession.getInstance().getUsername();

        // 1. Load Expense Pie Chart Data
        Map<String, Double> expenseTotals = transactionDAO.getCategoryWiseTotals(username, "EXPENSE");
        ObservableList<PieChart.Data> expenseData = FXCollections.observableArrayList();
        expenseTotals.forEach((category, total) -> {
            expenseData.add(new PieChart.Data(category + " (₹" + total + ")", total));
        });
        expenseChart.setData(expenseData);

        // 2. Load Income Pie Chart Data
        Map<String, Double> incomeTotals = transactionDAO.getCategoryWiseTotals(username, "INCOME");
        ObservableList<PieChart.Data> incomeData = FXCollections.observableArrayList();
        incomeTotals.forEach((category, total) -> {
            incomeData.add(new PieChart.Data(category + " (₹" + total + ")", total));
        });
        incomeChart.setData(incomeData);

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
        // This usually opens a new window, but for sidebar integration we can stick to the same pattern
        // or open the same dialog logic as MainController
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
