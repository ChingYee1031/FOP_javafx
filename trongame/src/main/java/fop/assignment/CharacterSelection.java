package fop.assignment;

import java.io.IOException;
import javafx.fxml.FXML;

/**
 * UI Controller for the Character Selection Screen.
 * Its job is to display stats and tell the App which character was picked.
 */
public class CharacterSelection {
    // --- UI COMPONENTS ---
    @FXML
    public void initialize() {
        // Automatically load stats from characters.txt when the screen opens
        refreshUI();
    }

    /**
     * Creates temporary character objects to read from the file 
     * and update the labels on the screen.
     */
    private void refreshUI() {
        // 1. Load Tron data for display
        Tron tempTron = new Tron();
        tempTron.loadAttributes("Tron");

        // 2. Load Kevin data for display
        Kevin tempKevin = new Kevin();
        tempKevin.loadAttributes("Kevin");
    }

    // --- BUTTON ACTIONS ---

    @FXML
    private void handleSelectTron() throws IOException {
        System.out.println("Tron Selected");
        
        // Create the actual character object for the game
        Tron selected = new Tron();
        selected.loadAttributes("Tron");
        
        // Save to the global variable in App.java
        App.chosenCharacter = selected; 
        
        startGame();
    }

    @FXML
    private void handleSelectKevin() throws IOException {
        System.out.println("Kevin Selected");
        
        // Create the actual character object for the game
        Kevin selected = new Kevin();
        selected.loadAttributes("Kevin");
        
        // Save to the global variable in App.java
        App.chosenCharacter = selected;
        
        startGame();
    }

    /**
     * Switch to the game arena.
     */
    private void startGame() throws IOException {
        // This assumes you have a setRoot method in your App.java
        App.setRoot("Arena"); 
    }
}