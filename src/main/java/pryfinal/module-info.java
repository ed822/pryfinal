module pryfinal {
    requires javafx.controls;
    requires javafx.fxml;

    opens pryfinal to javafx.fxml;
    exports pryfinal;
}