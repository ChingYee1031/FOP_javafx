module fop.assignment {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires javafx.media;

    opens fop.assignment to javafx.fxml;
    exports fop.assignment;
}
