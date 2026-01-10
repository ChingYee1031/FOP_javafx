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
    
    // The Overlay Box
    @FXML private VBox resetConfirmationOverlay;

    @FXML
    public void initialize() {
        // CHECK: Is there a valid player loaded?
        boolean canContinue = false;
        
        if (App.globalPlayer != null) {
            // Check for Progress (Level > 1 OR Tutorial Seen)
            boolean hasProgress = (App.globalPlayer.getLevel() > 1 || App.globalPlayer.hasSeenTutorial());

            // Check for "Game Over" state (Max Level & Max XP)
            // If they beat the game, they cannot continue; they must start over.
            boolean isGameFinished = (App.globalPlayer.getLevel() >= 99 && App.globalPlayer.getXP() >= 10000);

            // C. FINAL LOGIC: Enable ONLY if they have progress AND haven't finished the game
            if (hasProgress && !isGameFinished) {
                canContinue = true;
            }
        }

        // 2. APPLY STATE TO BUTTON
        if (canContinue) {
            // Enable "Continue"
            continueGameButton.setDisable(false);
            continueGameButton.setStyle("-fx-background-color: black; -fx-border-color: cyan; -fx-text-fill: cyan;");
        } else {
            // Disable "Continue"
            continueGameButton.setDisable(true);
            continueGameButton.setStyle("-fx-background-color: black; -fx-border-color: grey; -fx-text-fill: grey;");
        }
        
        // Ensure overlay is hidden at start
        if (resetConfirmationOverlay != null) {
            resetConfirmationOverlay.setVisible(false);
        }
    }

    @FXML
    private void handleStartNewGame() throws IOException {
        // 1. Check if they have progress to lose
        boolean hasProgress = false;
        if (App.globalPlayer != null && App.globalPlayer.getLevel() > 1) {
            hasProgress = true;
        }

        if (hasProgress) {
            // 2. SHOW OVERLAY: Ask for confirmation
            if (resetConfirmationOverlay != null) {
                resetConfirmationOverlay.setVisible(true);
            }
        } else {
            // 3. NO PROGRESS: Start immediately
            performResetAndStart();
        }
    }

    @FXML
    private void handleConfirmReset() throws IOException {
        // User clicked "YES" on the overlay
        performResetAndStart();
    }

    @FXML
    private void handleCancelReset() {
        // User clicked "NO" on the overlay
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

    // --- HELPER: The Actual Reset Logic ---
    private void performResetAndStart() throws IOException {
        if (App.globalPlayer != null) {
            // 1. Capture the Name (and Password if needed)
            String currentName = App.globalPlayer.getName();
            
            // 2. HARD RESET: Create a brand new Player object
            // This wipes Inventory, XP, Level, and Tutorial Flags completely.
            // We default to "Tron" (Cyan) settings, CharacterSelection will update color later.
            App.globalPlayer = new Player(currentName, "#00FFFF", 3.0, 1.5);
            
            // 3. Force Level 1 Explicitly
            App.globalPlayer.setLevel(1);
            App.globalPlayer.setXP(0);
            App.globalPlayer.setSeenTutorial(false);

            // 4. SAVE TO FILE IMMEDIATELY
            // Use globalPassword if your system requires it for saving
            if (App.globalPassword != null) {
                DataManager.savePlayer(App.globalPlayer, App.globalPassword);
            }
            
            System.out.println("RESET COMPLETE: Player is now Level " + App.globalPlayer.getLevel());
        }

        // 5. Proceed
        App.setRoot("CharacterSelection");
    }
}