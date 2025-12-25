package fop.assignment;

import java.io.IOException;

import javafx.fxml.FXML;

public class GameOverController {

    @FXML
    private void initialize() {
        // Optional: You could play a sad sound effect here
        System.out.println("Game Over Screen Loaded.");
    }

    @FXML
    private void handleReturnToMenu() throws IOException {
        System.out.println("Returning to Menu...");
        // We don't reset App.globalPlayer here so they keep their name/level
        // if they want to try again immediately.
        App.setRoot("MenuPage");
    }

    @FXML
    private void handleExit() {
        System.out.println("Exiting Game Application.");
        System.exit(0);
    }
}