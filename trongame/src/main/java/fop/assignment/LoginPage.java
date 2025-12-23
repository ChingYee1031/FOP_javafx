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

    // This ONE method handles everything when the button is clicked
    @FXML
    private void handleLogin() throws IOException {
        String input = nameInput.getText();

        // 1. Validation: Make sure they actually typed something
        if (input == null || input.trim().isEmpty()) {
            System.out.println("Error: Name cannot be empty!");
            return; // Stop here, don't change scenes
        }

        // 2. Save the name globally
        App.currentPlayerName = input; 
        System.out.println("Welcome, " + App.currentPlayerName);

        // 3. Move to the next page
        App.setRoot("MenuPage"); 
    }
}