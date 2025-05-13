module gid {
    requires javafx.controls;
    requires javafx.fxml;

    opens gid to javafx.fxml;
    exports gid;
}
