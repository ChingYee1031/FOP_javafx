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
    @FXML private VBox resetConfirmationOverlay;

    @FXML
    public void initialize() {
        // Is there a valid player loaded?
        boolean canContinue = false;
        
        if (App.globalPlayer != null) {
            // Check for Progress (Level > 1 OR Tutorial Seen)
            boolean hasProgress = (App.globalPlayer.getLevel() > 1 || App.globalPlayer.hasSeenTutorial());

            // Check for "Game Over" state (Max Level & Max XP)
            // If max lvl reached, they must start over.
            boolean isGameFinished = (App.globalPlayer.getLevel() >= 99 && App.globalPlayer.getXP() >= 10000);

            // FINAL LOGIC: Enable ONLY if they have progress & haven't finished the game
            if (hasProgress && !isGameFinished) {
                canContinue = true;
            }
        }

        // APPLY STATE TO BUTTON
        if (canContinue) {
            // Enable "Continue"
            continueGameButton.setDisable(false);
            continueGameButton.setStyle("-fx-background-color: black; -fx-border-color: cyan; -fx-text-fill: cyan;");
        } else {
            // Disable "Continue"
            continueGameButton.setDisable(true);
            continueGameButton.setStyle("-fx-background-color: black; -fx-border-color: grey; -fx-text-fill: grey;");
        }
        
        if (resetConfirmationOverlay != null) {
            resetConfirmationOverlay.setVisible(false);
        }
    }

    @FXML
    private void handleStartNewGame() throws IOException {
        // Check if they have progress to lose
        boolean hasProgress = false;
        if (App.globalPlayer != null && App.globalPlayer.getLevel() > 1) {
            hasProgress = true;
        }

        if (hasProgress) {
            // SHOW OVERLAY: Ask for confirmation
            if (resetConfirmationOverlay != null) {
                resetConfirmationOverlay.setVisible(true);
            }
        } else {
            // NO PROGRESS: Start immediately
            performResetAndStart();
        }
    }

    @FXML
    private void handleConfirmReset() throws IOException {
        // User clicked "YES" 
        performResetAndStart();
    }

    @FXML
    private void handleCancelReset() {
        // User clicked "NO" 
        if (resetConfirmationOverlay != null) {
            resetConfirmationOverlay.setVisible(false);
        }
    }

    @FXML
    private void handleContinueGame() throws IOException {
        System.out.println("Continuing Game...");
        App.setRoot("CharacterSelection");
    }

    @FXML
    private void handleLeaderboard() throws IOException {
        App.setRoot("LeaderboardPage");
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    // The Actual Reset Logic 
    private void performResetAndStart() throws IOException {
        if (App.globalPlayer != null) {
            // Capture the Name (and Password if needed)
            String currentName = App.globalPlayer.getName();
            
            // HARD RESET: Create a brand new Player object
            //default to "Tron" (Cyan) 
            App.globalPlayer = new Player(currentName, "#00FFFF", 3.0, 1.5);
            
            // Force Level 1 Explicitly (csv)
            App.globalPlayer.setLevel(1);
            App.globalPlayer.setXP(0);
            App.globalPlayer.setSeenTutorial(false);

            // SAVE TO FILE 
            if (App.globalPassword != null) {
                DataManager.savePlayer(App.globalPlayer, App.globalPassword);
            }
            
            System.out.println("RESET COMPLETE: Player is now Level " + App.globalPlayer.getLevel());
        }

        //Proceed
        App.setRoot("CharacterSelection");
    }
}