package fop.assignment;

import java.io.File;
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
    @FXML private Label lockMessage; 

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

        if (App.globalPlayer != null) {
            // FIX: DO NOT overwrite the Name. Only update visuals.
            App.globalPlayer.setColor("#0091ffff"); 
            // Tron Speed logic
            App.globalPlayer.setSpeed(1.5); 
        } else {
            // Guest Mode (No Save) - OK to set name here
            App.globalPlayer = new Player("Tron", "#0091ffff", 3.0, 1.5);
        }

        startGame();
    }

    @FXML
    private void handleSelectKevin() throws IOException {
        System.out.println("Kevin Selected");

        if (App.globalPlayer != null) {
             // FIX: DO NOT overwrite the Name. Only update visuals.
            App.globalPlayer.setColor("#ffffffff"); 
            
            // Kevin has slightly different stats
            App.globalPlayer.setSpeed(1.5); 
        } else {
            // Guest Mode (No Save)
            App.globalPlayer = new Player("Kevin", "#ffffffff", 3.0, 1.5);
        }

        startGame();
    }

    /**
     * Switch to the game arena.
     */
    private void startGame() throws IOException {
        // Logic: If the player is new (Level 1), show the Intro Story (Chapter 1).
        // If they are a veteran (Level > 1), skip straight to the Arena.
        
        if (App.globalPlayer.getLevel() == 1) {
            System.out.println("New Game detected. Playing Intro Cutscene...");
            App.goToCutscene("chapter1"); // <--- This loads the story first
        } else {
            System.out.println("Returning Player. Loading Arena directly...");
            App.setRoot("Arena"); // <--- Skip story for high-level players
        }
    }
}