package fop.assignment;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class CharacterSelection {

    @FXML private Label tronSpeedLabel;
    @FXML private Label tronNameLabel;
    @FXML private Button selectTronButton;

    @FXML private Label kevinSpeedLabel;
    @FXML private Label kevinNameLabel;
    @FXML private Button selectKevinButton;
    @FXML private Label lockMessage; // Ensure you have a Label in SceneBuilder for this

    protected double speed, lives;
    protected String color;

    @FXML
    public void initialize() {
        // Load Basic Stats
        loadStatsFromFile("Tron", tronSpeedLabel);
        loadStatsFromFile("Kevin", kevinSpeedLabel);

        // --- NEW: LOCKING MECHANIC ---
        // Check Global Player Level
        if (App.globalPlayer != null) {
            int level = App.globalPlayer.getLevel();
            
            if (level < 10) {
                // LOCK KEVIN
                selectKevinButton.setDisable(true);
                kevinNameLabel.setText("LOCKED (Lv 10)");
                if (lockMessage != null) lockMessage.setText("Reach Level 10 to unlock Kevin!");
            } else {
                // UNLOCK KEVIN
                selectKevinButton.setDisable(false);
                kevinNameLabel.setText("Kevin Flynn");
                if (lockMessage != null) lockMessage.setText("Character Unlocked!");
            }
        }
    }

    private void loadStatsFromFile(String targetName, Label speedLabel) {
        try {
            File file = new File("characters.txt");
            if (file.exists()) {
                Scanner reader = new Scanner(file);
                while (reader.hasNextLine()) {
                    String line = reader.nextLine();
                    String[] data = line.split(",");
                    if (data.length > 1 && data[0].equalsIgnoreCase(targetName)) {
                        speedLabel.setText("Speed: " + (data[2].contains("High") ? "Fast" : "Normal"));
                        break;
                    }
                }
                reader.close();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

@FXML
    private void handleSelectTron() throws IOException {
        System.out.println("Tron Selected");

        // FIX: Check if we already have a logged-in player
        if (App.globalPlayer != null) {
            // WE HAVE A SAVE FILE!
            // Don't create a new object. Just update the "skin" (Name/Color)
            // This PRESERVES your Level and XP.
            App.globalPlayer.setName("Tron");
            App.globalPlayer.setColor("#0091ffff"); 
            // Optional: Update stats if Tron has specific stats
            App.globalPlayer.setSpeed(1.5); 
            // DO NOT reset Level or XP here
        } else {
            // NO SAVE FILE (Guest Mode)
            // Create a fresh player
            App.globalPlayer = new Player("Tron", "#0091ffff", 3.0, 1.5);
        }

        // Start the game
        startGame();
    }

@FXML
    private void handleSelectKevin() throws IOException {
        System.out.println("Kevin Selected");

        // 1. Check if we have a saved game loaded
        if (App.globalPlayer != null) {
            // UPDATE EXISTING PLAYER (Keep Level/XP)
            App.globalPlayer.setName("Kevin");
            App.globalPlayer.setColor("#ffffffff"); // Orange Color for Kevin
            
            // Kevin has slightly different stats (e.g., slower but maybe better handling logic later)
            App.globalPlayer.setSpeed(1.5); 
        } else {
            // NEW GAME (No save file)
            App.globalPlayer = new Player("Kevin", "#ffffffff", 3.0, 1.5);
        }

        // 2. Start Game
        startGame();
    }

    private void updateGlobalCharacter(String name) {
        // We update the EXISTING global player instead of making a new one
        // This preserves the Level and XP
        if (App.globalPlayer != null) {
            // Update visuals only
            if (name.equals("Kevin")) {
                App.globalPlayer.setColor("#FFFFFF"); // White for Kevin
                // App.globalPlayer.setSpeed(2.5); // Optional speed boost
            } else {
                App.globalPlayer.setColor("#00FFFF"); // Blue for Tron
            }
        }
    }

    private void startGame() throws IOException {
        App.setRoot("Arena");
    }
}