package fop.assignment;

import java.io.IOException;
import java.util.Scanner;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * UI Controller for the Character Selection Screen.
 * Its job is to display stats and tell the App which character was picked.
 */
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
        updateGlobalCharacter("Tron");
        startGame();
    }

    @FXML
    private void handleSelectKevin() throws IOException {
        System.out.println("Kevin Selected");
        updateGlobalCharacter("Kevin");
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


    /**
     * Switch to the game arena.
     */
    private void startGame() throws IOException {
        App.setRoot("Arena");
    }
}