module fop.assignment {
    requires javafx.controls;
    requires javafx.fxml;

    opens fop.assignment to javafx.fxml;
    exports fop.assignment;
}
