package com.mybudg;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class CategoryManagementController implements Initializable {

    @FXML private HBox titleBar;
    @FXML private TextField newCategoryField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField budgetCategoryField;
    @FXML private TextField budgetAmountField;

    private CategoryDAO categoryDAO = new CategoryDAO();
    private BudgetDAO budgetDAO = new BudgetDAO(); // Need to add save budget method
    
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        typeCombo.setItems(FXCollections.observableArrayList("INCOME", "EXPENSE"));
        typeCombo.getSelectionModel().selectFirst();

        setupDragging();
    }

    private void setupDragging() {
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    @FXML
    private void handleAddCategory() {
        String name = newCategoryField.getText();
        String type = typeCombo.getValue();

        if (name == null || name.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Category name cannot be empty.");
            return;
        }

        if (categoryDAO.addCategory(name.trim(), type)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Category added successfully.");
            newCategoryField.clear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add category. It may already exist.");
        }
    }

    @FXML
    private void handleSetBudget() {
        String category = budgetCategoryField.getText();
        String amountText = budgetAmountField.getText();

        if (category == null || category.trim().isEmpty() || amountText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            // I need to implement a save budget method in BudgetDAO
            BudgetDAO bDao = new BudgetDAO();
            if (saveBudget(category, amount)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Budget set successfully.");
                budgetCategoryField.clear();
                budgetAmountField.clear();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to set budget.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid amount.");
        }
    }

    private boolean saveBudget(String category, double amount) {
        // Simple implementation for now, should be in BudgetDAO
        String username = UserSession.getInstance().getUsername();
        String query = "MERGE INTO BUDGET b USING (SELECT ? AS cat, ? AS usr FROM dual) s " +
                       "ON (b.CATEGORY = s.cat AND b.USERNAME = s.usr) " +
                       "WHEN MATCHED THEN UPDATE SET b.AMOUNT = ? " +
                       "WHEN NOT MATCHED THEN INSERT (USERNAME, CATEGORY, AMOUNT) VALUES (?, ?, ?)";
        
        try (java.sql.Connection conn = DatabaseConfig.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, category);
            pstmt.setString(2, username);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, username);
            pstmt.setString(5, category);
            pstmt.setDouble(6, amount);
            
            return pstmt.executeUpdate() > 0;
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void navigateTo(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) titleBar.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleViewDashboard() { navigateTo("main.fxml"); }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
