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
        // Auto-Save progress
        if (App.globalPlayer != null && App.globalPassword != null) {
            DataManager.savePlayer(App.globalPlayer, App.globalPassword);
            System.out.println("Progress Saved.");
        }
        App.setRoot("MenuPage");
    }

    @FXML
    private void handleExit() {
        System.out.println("Exiting Game Application.");
        System.exit(0);
    }
}