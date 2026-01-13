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
    private boolean isCreditsActive = false;  // State 3 
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

    //STAGE 1: STORY CUTSCENE 
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

    // STAGE 2: CONGRATULATIONS 
    private void showCongratulations() {
        try {
            File file = new File("images/tron_background.png");
            if (file.exists()) {
                backgroundImageView.setImage(new Image(file.toURI().toString()));
            }
            if (PressSpace != null) PressSpace.setVisible(true);

            // Update Text with USERNAME
            String playerName = "USER";
            if (App.globalPlayer != null) {
                playerName = App.globalPlayer.getName().toUpperCase(); // e.g. "JOHNDOE"
            }
            
            storyLabel.setText("CONGRATULATIONS, " + playerName + ".\nDESTINY FULFILLED.");
            
            // Adjust Layout
            storyLabel.setPrefHeight(300); 
            StackPane.setAlignment(storyLabel, Pos.CENTER); 

            isCutsceneActive = false;
            isCongratsActive = true;

            // Start 10s Timer
            activeTimer = new PauseTransition(Duration.seconds(10));
            activeTimer.setOnFinished(event -> showCredits()); 
            activeTimer.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //STAGE 3: CREDITS
    private void showCredits() {
        // Stop the timer if we skipped manually
        if (activeTimer != null) activeTimer.stop();

        String row1 = String.format("%-16s   %s", "GOH CHING YEE", "ONG JING ZHI");
        String row2 = String.format("%-16s   %s", "KU LEE HANN",   "YOW JIA YEN");
        String row3 = String.format("%-16s   %s", "YONG ZI YAN",   "VICTORIA KEW");

        // Set the text
        storyLabel.setText("CREDITS\n\n" +
                           row1 + "\n" +
                           row2 + "\n" +
                           row3 + "\n\n" +
                           "THANKS FOR PLAYING");
        
        storyLabel.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 28px; -fx-text-fill: white; -fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 10;");

        // Show "Press Space" instructions
        if (PressSpace != null) {
            PressSpace.setVisible(true);
            PressSpace.setText("[ PRESS SPACE TO RETURN TO MENU ]");
        }

        // Update State
        isCongratsActive = false;
        isCreditsActive = true;
    }

    private void returnToMenu() {
        try {
            //  Just Save (Do NOT reset to 0)
            if (App.globalPlayer != null && App.globalPassword != null) {
                // player  currently Lvl 99, XP 10000
                // save this state so Leaderboard sees highest score
                DataManager.savePlayer(App.globalPlayer, App.globalPassword);
            }

            // Stop Music and Switch
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
                returnToMenu(); 
            }
        }
    }
}