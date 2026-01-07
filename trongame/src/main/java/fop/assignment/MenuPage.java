package fop.assignment;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class MenuPage {

    @FXML private Button startNewGameButton;
    @FXML private Button continueGameButton;
    @FXML private Button leaderboardButton;
    @FXML private Button exitGameButton;
    
    // --- NEW: The Confirmation Overlay ---
    @FXML private VBox resetConfirmationOverlay;

    @FXML
    public void initialize() {
        // CHECK: Is this a new player?
        boolean hasProgress = false;
        
        if (App.globalPlayer != null) {
            if (App.globalPlayer.getLevel() > 1 || App.globalPlayer.hasSeenTutorial()) {
                hasProgress = true;
            }
        }

        if (!hasProgress) {
            continueGameButton.setDisable(true); // Disable Continue for new players
            continueGameButton.setStyle("-fx-background-color: black; -fx-border-color: grey; -fx-text-fill: grey;");
        }
    }

    @FXML
    private void handleStartNewGame() throws IOException {
        // 1. Check if they have progress to lose
        boolean hasProgress = (App.globalPlayer.getLevel() > 1);

        if (hasProgress) {
            // 2. SHOW CUSTOM OVERLAY (Instead of Alert)
            resetConfirmationOverlay.setVisible(true);
        } else {
            // No progress to lose, start immediately
            resetPlayerProgress();
            proceedToGame();
        }
    }

    // --- NEW: Overlay Button Handlers ---
    
    @FXML
    private void handleConfirmReset() throws IOException {
        // User clicked "YES"
        resetPlayerProgress();
        proceedToGame();
    }

    @FXML
    private void handleCancelReset() {
        // User clicked "NO"
        resetConfirmationOverlay.setVisible(false); // Hide the overlay
    }

    // ------------------------------------

    @FXML
    private void handleContinueGame() throws IOException {
        System.out.println("Continuing Game at Level " + App.globalPlayer.getLevel());
        proceedToGame();
    }

    private void resetPlayerProgress() {
        // Reset Logic
        App.globalPlayer.setLevel(1);
        App.globalPlayer.setXP(0);
        App.globalPlayer.setSeenTutorial(false); 
        
        // Save the reset state immediately
        DataManager.savePlayer(App.globalPlayer, App.globalPassword);
        System.out.println("Player Progress Reset to Level 1.");
    }

    private void proceedToGame() throws IOException {
        App.setRoot("CharacterSelection");
    }

    @FXML
    private void handleLeaderboard() throws IOException {
        System.out.println("Going to Leaderboard...");
        App.setRoot("LeaderboardPage");
    }

    @FXML
    private void handleExit() {
        System.out.println("Exiting Game...");
        System.exit(0);
    }
}