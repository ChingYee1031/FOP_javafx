package fop.assignment;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class LoginPage {
    
    @FXML
    private TextField nameInput;

    @FXML
    private Button loginButton;

    @FXML
    private void handleLogin() throws IOException {
        String input = nameInput.getText();

        // 1. Validation
        if (input == null || input.trim().isEmpty()) {
            System.out.println("Error: Name cannot be empty!");
            return; 
        }

        // 2. FIX: Create the Player Object immediately
        // We initialize them with default "Tron" stats (Blue, 3 lives, 1.5 speed)
        // (If they pick a different character later, we can update these stats then)
        Player newPlayer = new Player(input, "#00FFFF", 3.0, 1.5);

        // 3. FIX: Save to the new 'globalPlayer' variable instead of 'currentPlayerName'
        App.globalPlayer = newPlayer; 
        System.out.println("Welcome, " + newPlayer.getName());

        // 4. Move to next page
        // Note: If you want to start the Story immediately, change "MenuPage" to "CutscenePage"
        App.setRoot("MenuPage"); 
    }
}