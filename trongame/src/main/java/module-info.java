module fop.assignment {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    opens fop.assignment to javafx.fxml;
    exports fop.assignment;
}
