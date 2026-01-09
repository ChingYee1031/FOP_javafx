package fop.assignment;

import java.io.File;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class EndingPage {

    @FXML private StackPane rootPane;
    @FXML private ImageView backgroundImageView;
    @FXML private Button tronEndingButton;
    @FXML private Button kevinEndingButton;
    @FXML private Label storyLabel;
    @FXML private Label PressSpace; 

    // --- STATES ---
    private boolean isCutsceneActive = false; // State 1
    private boolean isCongratsActive = false; // State 2
    private boolean isCreditsActive = false;  // State 3 (New)

    // Helper to control the timer (so we can stop it if user skips)
    private PauseTransition activeTimer;

    @FXML
    public void initialize() {
        rootPane.setFocusTraversable(true);

        SoundManager.playMusic("endingbgm.mp3");
    }

    @FXML
    private void handleTronEndingButton() {
        showCutscene("images/ending_villain.scene1.png", 
                     "UNAUTHORIZED ACCESS TERMINATED.\nTHE GRID IS SAFE ONCE MORE.");
    }

    @FXML
    private void handleKevinEndingButton() {
        showCutscene("images/ending_hero.scene1.png", 
                     "SYSTEM LIBERATED.\nTHE CREATOR RETURNS.");
    }

    // --- STAGE 1: STORY CUTSCENE ---
    private void showCutscene(String imageName, String storyText) {
        try {
            File file = new File(imageName);
            if (file.exists()) {
                backgroundImageView.setImage(new Image(file.toURI().toString()));
                
                tronEndingButton.setVisible(false);
                kevinEndingButton.setVisible(false);

                storyLabel.setText(storyText);
                storyLabel.setVisible(true);
                
                if (PressSpace != null) PressSpace.setVisible(true);

                isCutsceneActive = true;
                rootPane.requestFocus();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- STAGE 2: CONGRATULATIONS ---
    private void showCongratulations() {
        try {
            // 1. Change Background
            File file = new File("images/tron_background.png");
            if (file.exists()) {
                backgroundImageView.setImage(new Image(file.toURI().toString()));
            }

            // 2. Hide "Press Space" initially (optional, or keep it if you want them to skip)
            if (PressSpace != null) PressSpace.setVisible(false);

            // 3. Update Text with USERNAME
            String playerName = "USER"; // Default fallback
            if (App.globalPlayer != null) {
                playerName = App.globalPlayer.getName().toUpperCase(); // e.g. "JOHNDOE"
            }
            
            storyLabel.setText("CONGRATULATIONS, " + playerName + ".\nDESTINY FULFILLED.");
            
            // Adjust Layout
            storyLabel.setPrefHeight(300); 
            StackPane.setAlignment(storyLabel, Pos.CENTER); 

            // 4. Update State
            isCutsceneActive = false;
            isCongratsActive = true;

            // 5. Start 10s Timer -> Then go to CREDITS (not menu yet)
            activeTimer = new PauseTransition(Duration.seconds(10));
            activeTimer.setOnFinished(event -> showCredits()); // Chain to Credits
            activeTimer.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- STAGE 3: CREDITS ---
    private void showCredits() {
        // Stop the timer if we skipped manually
        if (activeTimer != null) activeTimer.stop();

        // 1. Format the names into aligned columns
        // %-16s means "take up 16 characters space, aligned to the left"
        // This ensures every line has the exact same width structure.
        String row1 = String.format("%-16s   %s", "GOH CHING YEE", "ONG JING ZHI");
        String row2 = String.format("%-16s   %s", "KU LEE HANN",   "YOW JIA YEN");
        String row3 = String.format("%-16s   %s", "YONG ZI YAN",   "VICTORIA KEW");

        // 2. Set the text
        storyLabel.setText("CREDITS\n\n" +
                           row1 + "\n" +
                           row2 + "\n" +
                           row3 + "\n\n" +
                           "THANKS FOR PLAYING");
        
        // 3. Optional: Force a strict Monospace font for perfect alignment
        // You can remove this line if you prefer to keep the OCR font
        storyLabel.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 28px; -fx-text-fill: white; -fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 10;");

        // 4. Show "Press Space" instructions
        if (PressSpace != null) {
            PressSpace.setVisible(true);
            PressSpace.setText("[ PRESS SPACE TO RETURN TO MENU ]");
        }

        // 5. Update State
        isCongratsActive = false;
        isCreditsActive = true;
    }

    // inside EndingPage.java

    private void returnToMenu() {
        try {
            // 1. Just Save (Do NOT reset to 0)
            if (App.globalPlayer != null && App.globalPassword != null) {
                // The player is currently Lvl 99, XP 10000. 
                // We save this state so the Leaderboard sees the high score.
                DataManager.savePlayer(App.globalPlayer, App.globalPassword);
            }

            // 2. Stop Music and Switch
            SoundManager.stopMusic();
            App.setRoot("MenuPage");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.ENTER) {
            if (isCutsceneActive) {
                showCongratulations();
            } else if (isCongratsActive) {
                showCredits();
            } else if (isCreditsActive) {
                returnToMenu(); // <--- Call the save/reset logic here
            }
        }
    }
}