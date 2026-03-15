package com.mybudg;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class MainController implements Initializable {

    @FXML private HBox titleBar;
    @FXML private Label welcomeLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpenseLabel;
    @FXML private Label balanceLabel;

    @FXML private TableView<Transaction> recentTransactionTable;
    @FXML private TableColumn<Transaction, LocalDate> recentDateColumn;
    @FXML private TableColumn<Transaction, String> recentCategoryColumn;
    @FXML private TableColumn<Transaction, Double> recentAmountColumn;
    @FXML private TableColumn<Transaction, String> recentTypeColumn;

    @FXML private VBox incomeCard;
    @FXML private VBox expenseCard;
    @FXML private VBox balanceCard;

    @FXML private HBox headerSection;
    @FXML private HBox cardsSection;
    @FXML private VBox tableSection;
    @FXML private VBox sidebar;

    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isMaximized = false;
    private double lastWidth = 900;
    private double lastHeight = 650;
    private double lastX = 0;
    private double lastY = 0;

    private TransactionDAO transactionDAO = new TransactionDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Handle window dragging
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            if (stage.isMaximized()) return; // Don't drag while maximized
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        // Toggle maximize on double click
        titleBar.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                toggleMaximize();
            }
        });

        // Initialize Recent Table Columns
        recentDateColumn.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        recentDateColumn.setCellFactory(column -> new TableCell<Transaction, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DateUtils.format(item));
                }
            }
        });
        
        recentCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        recentAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        recentTypeColumn.setCellValueFactory(new PropertyValueFactory<>("transactionType"));

        // Custom Cell Factory for Type Column (Red/Green)
        recentTypeColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    if (item.equalsIgnoreCase("EXPENSE")) {
                        getStyleClass().add("type-expense");
                        getStyleClass().remove("type-income");
                    } else {
                        getStyleClass().add("type-income");
                        getStyleClass().remove("type-expense");
                    }
                }
            }
        });

        // Set Welcome Message
        UserSession session = UserSession.getInstance();
        if (session != null) {
            welcomeLabel.setText("Welcome back, " + UserSession.getInstance().getUsername() + "!");
        }

        refreshDashboard();
        startClock();
        playEntranceAnimation();
        setupCardHoverAnimations();
    }

    private void setupCardHoverAnimations() {
        Node[] cards = {incomeCard, expenseCard, balanceCard};
        for (Node card : cards) {
            card.setOnMouseEntered(e -> {
                TranslateTransition tt = new TranslateTransition(Duration.millis(200), card);
                tt.setToY(-5);
                
                FadeTransition ft = new FadeTransition(Duration.millis(200), card);
                ft.setToValue(1.0);
                
                // Scale effect
                card.setScaleX(1.03);
                card.setScaleY(1.03);
                
                new ParallelTransition(tt).play();
            });
            
            card.setOnMouseExited(e -> {
                TranslateTransition tt = new TranslateTransition(Duration.millis(200), card);
                tt.setToY(0);
                
                card.setScaleX(1.0);
                card.setScaleY(1.0);
                
                new ParallelTransition(tt).play();
            });
        }
    }

    private void playEntranceAnimation() {
        // Initial state: Hidden and slightly shifted down
        Node[] nodes = {sidebar, headerSection, cardsSection, tableSection};
        for (Node node : nodes) {
            node.setOpacity(0);
            if (node == sidebar) {
                node.setTranslateX(-50);
            } else {
                node.setTranslateY(20);
            }
        }

        // Sequential animation
        Timeline timeline = new Timeline();
        for (int i = 0; i < nodes.length; i++) {
            final Node node = nodes[i];
            KeyFrame kf = new KeyFrame(Duration.millis(i * 150), e -> {
                FadeTransition ft = new FadeTransition(Duration.millis(600), node);
                ft.setToValue(1.0);

                TranslateTransition tt = new TranslateTransition(Duration.millis(600), node);
                if (node == sidebar) {
                    tt.setFromX(-50);
                    tt.setToX(0);
                } else {
                    tt.setFromY(20);
                    tt.setToY(0);
                }

                ParallelTransition pt = new ParallelTransition(ft, tt);
                pt.play();
            });
            timeline.getKeyFrames().add(kf);
        }
        timeline.play();
    }

    public void refreshDashboard() {
        String username = UserSession.getInstance().getUsername();
        double totalIncome = transactionDAO.getTotalAmountByType(username, "INCOME");
        double totalExpense = transactionDAO.getTotalAmountByType(username, "EXPENSE");
        double balance = totalIncome - totalExpense;

        totalIncomeLabel.setText(String.format("₹%.2f", totalIncome));
        totalExpenseLabel.setText(String.format("₹%.2f", totalExpense));
        balanceLabel.setText(String.format("₹%.2f", balance));

        if (balance < 0) {
            balanceLabel.setStyle("-fx-text-fill: #e74c3c;");
        } else {
            balanceLabel.setStyle("-fx-text-fill: #2ecc71;");
        }

        // Load recent transactions (limit to last 5-10)
        java.util.List<Transaction> all = transactionDAO.getAllTransactions(username);
        ObservableList<Transaction> recent = FXCollections.observableArrayList(
                all.subList(0, Math.min(all.size(), 10))
        );
        recentTransactionTable.setItems(recent);
    }

    private void startClock() {
        updateDateTime();
        Timeline clock = new Timeline(new KeyFrame(Duration.minutes(1), e -> updateDateTime()));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    private void updateDateTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy | hh:mm a");
        dateTimeLabel.setText(LocalDateTime.now().format(dtf));
    }

    @FXML
    private void closeWindow(ActionEvent event) {
        System.exit(0);
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
    private void minimizeWindow(ActionEvent event) {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        stage.setIconified(true);
    }

    private void navigateTo(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) titleBar.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) throws IOException {
        UserSession.cleanUserSession();
        Stage stage = (Stage) titleBar.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    @FXML
    private void handleAddTransaction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("add-transaction.fxml"));
            Parent root = loader.load();
            
            AddTransactionController controller = loader.getController();
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            
            // Allow dragging for the new window
            root.setOnMousePressed(e -> {
                xOffset = e.getSceneX();
                yOffset = e.getSceneY();
            });
            root.setOnMouseDragged(e -> {
                stage.setX(e.getScreenX() - xOffset);
                stage.setY(e.getScreenY() - yOffset);
            });

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewDashboard() {
        navigateTo("main.fxml");
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
    private void handleManageCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("category-management.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            
            root.setOnMousePressed(e -> {
                xOffset = e.getSceneX();
                yOffset = e.getSceneY();
            });
            root.setOnMouseDragged(e -> {
                stage.setX(e.getScreenX() - xOffset);
                stage.setY(e.getScreenY() - yOffset);
            });

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSettings() {
        navigateTo("settings.fxml");
    }
}
