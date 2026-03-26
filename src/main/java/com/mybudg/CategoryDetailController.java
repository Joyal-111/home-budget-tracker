package com.mybudg;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CategoryDetailController implements Initializable {

    @FXML private HBox titleBar;
    @FXML private Label pageTitle;
    @FXML private PieChart categoryPieChart;
    @FXML private TableView<CategoryTotal> categoryTable;
    @FXML private TableColumn<CategoryTotal, String> categoryColumn;
    @FXML private TableColumn<CategoryTotal, Double> amountColumn;
    @FXML private VBox chartContainer;
    @FXML private VBox tableContainer;

    private TransactionDAO transactionDAO = new TransactionDAO();
    private String transactionType; // "INCOME" or "EXPENSE"
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Window dragging
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        // Initialize Table Columns
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        
        // Format amount column
        amountColumn.setCellFactory(column -> new javafx.scene.control.TableCell<CategoryTotal, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("₹%.2f", item));
                }
            }
        });
    }

    public void setTransactionType(String type) {
        this.transactionType = type;
        pageTitle.setText(type.equalsIgnoreCase("INCOME") ? "Income Category Breakdown" : "Expense Category Breakdown");
        loadData();
    }

    private void loadData() {
        String username = UserSession.getInstance().getUsername();
        Map<String, Double> totals = transactionDAO.getCategoryWiseTotals(username, transactionType);
        
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        ObservableList<CategoryTotal> tableData = FXCollections.observableArrayList();
        
        totals.forEach((category, amount) -> {
            pieData.add(new PieChart.Data(category, amount));
            tableData.add(new CategoryTotal(category, amount));
        });
        
        categoryPieChart.setData(pieData);
        categoryTable.setItems(tableData);
        
        double totalAmount = getTotal(totals);
        if (totalAmount > 0) {
            for (PieChart.Data data : categoryPieChart.getData()) {
                data.setName(data.getName() + " (" + String.format("%.1f%%", (data.getPieValue() / totalAmount * 100)) + ")");
            }
        }
    }

    private double getTotal(Map<String, Double> totals) {
        return totals.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
            titleBar.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void closeWindow() {
        System.exit(0);
    }

    @FXML
    private void minimizeWindow() {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        stage.setIconified(true);
    }

    // Inner class for TableView
    public static class CategoryTotal {
        private final String category;
        private final double amount;

        public CategoryTotal(String category, double amount) {
            this.category = category;
            this.amount = amount;
        }

        public String getCategory() { return category; }
        public double getAmount() { return amount; }
    }
}
