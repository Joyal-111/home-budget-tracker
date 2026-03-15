module com.mybudg {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.oracle.database.jdbc;

    opens com.mybudg to javafx.fxml;
    exports com.mybudg;
}
