package fop.assignment;

import java.io.IOException;

import javafx.fxml.FXML;

public class GameOverController {

    @FXML
    private void initialize() {
        System.out.println("Game Over Screen Loaded.");
        // Optional: Play sad sound here if you want
    }

    @FXML
    private void handleReturnToMenu() throws IOException {
        saveProgress();
        App.setRoot("MenuPage");
    }

    // --- NEW: RESTART BUTTON LOGIC ---
    @FXML
    private void handleRestart() throws IOException {
        System.out.println("Player chose to Restart.");
        
        saveProgress(); // Save XP/Levels gained before death
        
        // Go directly back to the Arena
        // (ArenaController.java will automatically reset lives to 3 upon loading)
        App.setRoot("Arena");
    }

    // Helper method to keep code clean
    private void saveProgress() {
        if (App.globalPlayer != null && App.globalPassword != null) {
            DataManager.savePlayer(App.globalPlayer, App.globalPassword);
            System.out.println("Progress Auto-Saved.");
        }
    }
}