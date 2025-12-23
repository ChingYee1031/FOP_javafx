package fop.assignment;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

public class CharacterSelection {

    // --- UI COMPONENTS ---
    @FXML private Label tronSpeedLabel;
    @FXML private Label tronHandlingLabel;
    @FXML private Label tronNameLabel;

    @FXML private Label kevinSpeedLabel;
    @FXML private Label kevinHandlingLabel;
    @FXML private Label kevinNameLabel;

    @FXML private Button selectTronButton;
    @FXML private Button selectKevinButton;

    // --- ATTRIBUTES (From Character.java) ---
    protected String name, color, handling;
    protected double speed, lives;
    protected int experiencePoints;

    @FXML
    public void initialize() {
        // Automatically load stats from file when the screen opens
        loadStatsFromFile();
    }

    private void loadStatsFromFile() {
        // Load data for the character "Tron"
        loadAttributes("Tron");
        
        // Update UI labels with the loaded data
        tronSpeedLabel.setText("Speed: " + this.speed);
        tronNameLabel.setText("Character: Tron");
    }

    /**
     * Integrated logic to load data from characters.txt
     */
    public void loadAttributes(String targetName) {
        // Default values
        this.color = "#00FF00"; 

        try {
            File file = new File("characters.txt");
            if (!file.exists()) {
                System.err.println("CRITICAL: characters.txt missing from " + file.getAbsolutePath());
                return;
            }

            Scanner reader = new Scanner(file);
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                String[] data = line.split(",");

                if (data.length > 1 && data[0].equalsIgnoreCase(targetName)) {
                    // Logic to map color names to Hex codes
                    if (data[1].equalsIgnoreCase("Blue")) {
                        this.color = "#0000FF";
                    } else if (data[1].equalsIgnoreCase("White")) {
                        this.color = "#FFFFFF";
                    }

                    // Logic for Speed: "High" sets speed to 2.5, otherwise 1.5
                    this.speed = data[2].contains("High") ? 2.5 : 1.5;
                    // Parse lives from the 5th column
                    this.lives = Double.parseDouble(data[4]);
                    break;
                }
            }
            reader.close();
        } catch (Exception e) {
            System.err.println("File error: " + e.getMessage());
        }
    }

    public String getColor() {
        return this.color;
    }

    @FXML
    private void handleSelectTron() throws IOException {
        System.out.println("Tron Selected");
        // App class handles the global state and scene switching
        // App.selectedCharacter = "Tron"; 

        Tron tron = new Tron();
        tron.loadAttributes("Tron");
        App.globalSelectedCharacter = tron;

        startGame();
    }

    @FXML
    private void handleSelectKevin() throws IOException {
        System.out.println("Kevin Selected");
        // App.selectedCharacter = "Kevin"; 

        Kevin kevin = new Kevin();
        kevin.loadAttributes("Kevin");
        App.globalSelectedCharacter = kevin;
        
        startGame();
    }

    private void startGame() throws IOException {
        // Switch to the game arena or cutscene
        App.setRoot("Arena");
    }
}