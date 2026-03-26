package com.mybudg;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class TransactionsController implements Initializable {

    @FXML private HBox titleBar;
    @FXML private javafx.scene.control.ListView<Transaction> transactionListView;
    @FXML private javafx.scene.control.Label transactionCountLabel;
    @FXML private TextField searchField;
    @FXML private DatePicker dateFilter;
    @FXML private ComboBox<String> typeFilter;
    @FXML private ComboBox<String> sortFilter;

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isMaximized = false;
    private double lastWidth = 900;
    private double lastHeight = 650;
    private double lastX = 0;
    private double lastY = 0;
    private static int lastAddedId = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

        typeFilter.setItems(FXCollections.observableArrayList("All", "EXPENSE", "INCOME"));
        sortFilter.setItems(FXCollections.observableArrayList("Date (Newest)", "Date (Oldest)", "Amount (High-Low)", "Amount (Low-High)", "Income First", "Expense First"));
        sortFilter.getSelectionModel().selectFirst();

        DateUtils.setupDatePicker(dateFilter);
        setupListView();
        loadTransactions();

        // Add filter listeners
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        dateFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        typeFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        sortFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        String username = UserSession.getInstance().getUsername();
        java.util.List<Transaction> all = transactionDAO.getAllTransactions(username);
        
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        LocalDate filterDate = dateFilter.getValue();
        String filterType = typeFilter.getValue();
        String sortBy = sortFilter.getValue();

        java.util.stream.Stream<Transaction> stream = all.stream()
            .filter(t -> {
                boolean matchesSearch = searchText.isEmpty() || 
                                        (t.getCategory() != null && t.getCategory().toLowerCase().contains(searchText)) || 
                                        (t.getDescription() != null && t.getDescription().toLowerCase().contains(searchText)) ||
                                        (t.getTransactionType() != null && t.getTransactionType().toLowerCase().contains(searchText)) ||
                                        (t.getTransactionDate() != null && DateUtils.format(t.getTransactionDate()).contains(searchText)) ||
                                        (String.valueOf(t.getAmount()).contains(searchText));
                
                boolean matchesDate = filterDate == null || t.getTransactionDate().equals(filterDate);
                boolean matchesType = filterType == null || filterType.equals("All") || t.getTransactionType().equalsIgnoreCase(filterType);
                
                return matchesSearch && matchesDate && matchesType;
            });

        if (sortBy != null) {
            switch (sortBy) {
                case "Date (Newest)": 
                    stream = stream.sorted(Comparator.comparing(Transaction::getTransactionDate).reversed()); 
                    break;
                case "Date (Oldest)": 
                    stream = stream.sorted(Comparator.comparing(Transaction::getTransactionDate)); 
                    break;
                case "Amount (High-Low)": 
                    stream = stream.sorted(Comparator.comparingDouble(Transaction::getAmount).reversed()); 
                    break;
                case "Amount (Low-High)": 
                    stream = stream.sorted(Comparator.comparingDouble(Transaction::getAmount)); 
                    break;
                case "Income First": 
                    stream = stream.sorted(Comparator.comparing(Transaction::getTransactionType).reversed()); 
                    break;
                case "Expense First": 
                    stream = stream.sorted(Comparator.comparing(Transaction::getTransactionType)); 
                    break;
            }
        }

        ObservableList<Transaction> filteredList = FXCollections.observableArrayList(stream.collect(Collectors.toList()));
        transactionListView.setItems(filteredList);
        transactionCountLabel.setText("Count: " + filteredList.size() + " transactions");
    }

    private void setupListView() {
        transactionListView.setCellFactory(param -> new javafx.scene.control.ListCell<Transaction>() {
            @Override
            protected void updateItem(Transaction item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox card = new HBox(10);
                    card.getStyleClass().add("transaction-card-row");
                    if (item.getTransactionId() == lastAddedId) {
                        card.getStyleClass().add("highlight-card");
                    }
                    card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    javafx.scene.control.CheckBox checkBox = new javafx.scene.control.CheckBox();
                    checkBox.setMinWidth(50);

                    javafx.scene.control.Label dateLbl = new javafx.scene.control.Label(DateUtils.format(item.getTransactionDate()));
                    dateLbl.setMinWidth(120);

                    javafx.scene.control.Label typeLbl = new javafx.scene.control.Label(item.getTransactionType().toUpperCase());
                    typeLbl.getStyleClass().add("type-pill");
                    if (item.getTransactionType().equalsIgnoreCase("EXPENSE")) {
                        typeLbl.getStyleClass().add("type-pill-expense");
                    } else {
                        typeLbl.getStyleClass().add("type-pill-income");
                    }
                    typeLbl.setMinWidth(100);
                    typeLbl.setAlignment(javafx.geometry.Pos.CENTER);

                    String emoji = getCategoryEmoji(item.getCategory());
                    javafx.scene.control.Label catLbl = new javafx.scene.control.Label(emoji + " " + item.getCategory());
                    catLbl.setMinWidth(150);

                    javafx.scene.control.Label amountLbl = new javafx.scene.control.Label(String.format("%.2f", item.getAmount()));
                    amountLbl.getStyleClass().add("amount-text-bold");
                    amountLbl.setMinWidth(120);

                    javafx.scene.control.Label descLbl = new javafx.scene.control.Label(item.getDescription());
                    descLbl.setWrapText(true);
                    HBox.setHgrow(descLbl, javafx.scene.layout.Priority.ALWAYS);

                    card.getChildren().addAll(checkBox, dateLbl, typeLbl, catLbl, amountLbl, descLbl);
                    setGraphic(card);
                }
            }
        });
    }

    private String getCategoryEmoji(String category) {
        if (category == null) return "📦";
        switch (category.toLowerCase()) {
            case "food": return "🍔";
            case "rent": case "housing": return "🏠";
            case "shopping": return "🛍️";
            case "transport": case "travel": return "🚗";
            case "salary": case "income": return "💰";
            case "health": return "🏥";
            case "entertainment": return "🎬";
            case "electricity": return "⚡";
            case "utilities": return "💡";
            default: return "📦";
        }
    }

    private void loadTransactions() {
        applyFilters();
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        dateFilter.setValue(null);
        typeFilter.setValue("All");
        sortFilter.setValue("Date (Newest)");
        loadTransactions();
    }
    
    public static void setLastAddedId(int id) {
        lastAddedId = id;
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        Transaction selected = transactionListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText("Delete Transaction");
            alert.setContentText("Are you sure you want to delete this transaction?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                if (transactionDAO.deleteTransaction(selected.getTransactionId())) {
                    loadTransactions();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setContentText("Failed to delete transaction.");
                    errorAlert.show();
                }
            }
        } else {
            Alert warnAlert = new Alert(Alert.AlertType.WARNING);
            warnAlert.setContentText("Please select a transaction to delete.");
            warnAlert.show();
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
