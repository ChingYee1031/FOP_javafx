package fop.assignment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

public class CharacterSelection {

    // TRON LABELS
    @FXML private Label tronSpeedLabel;
    @FXML private Label tronHandlingLabel;
    @FXML private Label tronNameLabel;

    // KEVIN LABELS
    @FXML private Label kevinSpeedLabel;
    @FXML private Label kevinHandlingLabel;
    @FXML private Label kevinNameLabel;

    // BUTTON
    @FXML
    private Button selectTronButton;
    @FXML
    private Button selectKevinButton;

    @FXML
    public void initialize() {
        // Automatically load stats from file when the screen opens
        loadStatsFromFile();
    }

    private void loadStatsFromFile() {
        try {
            Scanner scanner = new Scanner(new File("characters.txt"));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(","); // Splits "Tron,Blue,5,5,3"
                String name = parts[0].trim();
                String speed = parts[2].trim();
                String handling = parts[3].trim();

                // Check which character this line is for and update their labels
                if (name.equalsIgnoreCase("Tron")) {
                    tronSpeedLabel.setText("Speed: " + speed);
                    tronHandlingLabel.setText("Handling: " + handling);
                    // tronLifeLabel.setText("Lives: " + lives); // Uncomment if you added this label
                } 
                else if (name.equalsIgnoreCase("Kevin")) {
                    kevinSpeedLabel.setText("Speed: " + speed);
                    kevinHandlingLabel.setText("Handling: " + handling);
                    // kevinLifeLabel.setText("Lives: " + lives); // Uncomment if you added this label
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error: characters.txt not found!");
        }
    }

    @FXML
    private void handleSelectTron() throws IOException {
        System.out.println("Tron Selected");
        App.selectedCharacter = "Tron"; // Save choice globally
        startGame();
    }

    @FXML
    private void handleSelectKevin() throws IOException {
        System.out.println("Kevin Selected");
        App.selectedCharacter = "Kevin"; // Save choice globally
        startGame();
    }

    private void startGame() throws IOException {
        // Switch to the actual game arena
        App.setRoot("CutscenePage");  
    }
}
