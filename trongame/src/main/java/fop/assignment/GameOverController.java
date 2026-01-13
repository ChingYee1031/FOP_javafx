package fop.assignment;

import java.io.IOException;
import javafx.fxml.FXML;
public class GameOverController {

    @FXML
    private void initialize() {
        System.out.println("Game Over Screen Loaded.");
    }

    @FXML
    private void handleReturnToMenu() throws IOException {
        saveProgress();
        App.setRoot("MenuPage");
    }

    // RESTART BUTTON LOGIC
    @FXML
    private void handleRestart() throws IOException {
        System.out.println("Player chose to Restart.");
        
        saveProgress(); // Save XP/Levels gained before death
        
        // Go directly back to the Arena
        App.setRoot("Arena");
    }

    private void saveProgress() {
        if (App.globalPlayer != null && App.globalPassword != null) {
            DataManager.savePlayer(App.globalPlayer, App.globalPassword);
            System.out.println("Progress Auto-Saved.");
        }
    }
}