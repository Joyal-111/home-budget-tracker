package com.mybudg;

import java.time.LocalDate;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddTransactionController {

    @FXML private RadioButton incomeRadio;
    @FXML private RadioButton expenseRadio;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextArea descriptionArea;

    private TransactionDAO transactionDAO = new TransactionDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    private MainController mainController;

    @FXML
    public void initialize() {
        DateUtils.setupDatePicker(datePicker); // Configure custom format
        datePicker.setValue(LocalDate.now());
        updateCategories();

        incomeRadio.setOnAction(e -> updateCategories());
        expenseRadio.setOnAction(e -> updateCategories());

        // Amount field validation - only numbers and dot
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                amountField.setText(oldValue);
            }
        });
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void updateCategories() {
        String type = incomeRadio.isSelected() ? "INCOME" : "EXPENSE";
        List<String> categories = categoryDAO.getCategoriesByType(type);
        categoryCombo.setItems(FXCollections.observableArrayList(categories));
        if (!categories.isEmpty()) {
            categoryCombo.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String amountText = amountField.getText();
        String category = categoryCombo.getValue();
        
        // Ensure manual text input in DatePicker is committed to the value property
        if (datePicker.getEditor().getText() != null && !datePicker.getEditor().getText().isEmpty()) {
            try {
                LocalDate parsedDate = DateUtils.parse(datePicker.getEditor().getText());
                if (parsedDate != null) {
                    datePicker.setValue(parsedDate);
                }
            } catch (Exception e) {
                // If parsing fails, fall back to existing value or let validation handle it
            }
        }
        LocalDate date = datePicker.getValue();

        if (amountText.isEmpty() || category == null || date == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all required fields.");
            return;
        }

        double amount = Double.parseDouble(amountText);
        String type = incomeRadio.isSelected() ? "INCOME" : "EXPENSE";
        String description = descriptionArea.getText();

        // Budget Check for Expenses
        if ("EXPENSE".equals(type)) {
            BudgetDAO budgetDAO = new BudgetDAO();
            String username = UserSession.getInstance().getUsername();
            double budgetLimit = budgetDAO.getBudgetAmount(username, category);
            
            if (budgetLimit > 0) {
                String currentMonth = date.format(java.time.format.DateTimeFormatter.ofPattern("YYYY-MM"));
                double currentSpent = budgetDAO.getCurrentSpending(username, category, currentMonth);
                
                if (currentSpent + amount > budgetLimit) {
                    showAlert(Alert.AlertType.WARNING, "Budget Warning", 
                        "Adding this expense will exceed your budget for " + category + "!\n" +
                        "Budget: " + budgetLimit + "\n" +
                        "Spent so far: " + currentSpent + "\n" +
                        "Total after this: " + (currentSpent + amount));
                }
            }
        }

        Transaction transaction = new Transaction(
            UserSession.getInstance().getUsername(),
            amount,
            category,
            description,
            type,
            date
        );

        int savedId = transactionDAO.saveTransaction(transaction);
        if (savedId > 0) {
            TransactionsController.setLastAddedId(savedId);
            if (mainController != null) {
                mainController.refreshDashboard();
            }
            closeWindow(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save transaction.");
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow(event);
    }

    @FXML
    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void minimizeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
