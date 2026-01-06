package fop.assignment;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginPage {
    
    @FXML private TextField nameInput;
    @FXML private PasswordField passInput; 
    @FXML private Label errorLabel; 

    @FXML
    private void handleLogin() throws IOException {
        String username = nameInput.getText().trim();
        String password = passInput.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("System Error: Inputs cannot be empty.");
            return;
        }

        // --- FIX: Prevent CSV Corruption ---
        if (username.contains(",") || password.contains(",")) {
            errorLabel.setText("Error: Fields cannot contain commas (',').");
            return;
        }

        // 1. Attempt Login
        Player loadedPlayer = DataManager.login(username, password);

        if (loadedPlayer != null) {
            // LOGIN SUCCESS
            App.globalPlayer = loadedPlayer;
            App.globalPassword = password; 
            System.out.println("User Recognized. Level: " + loadedPlayer.getLevel());
        } else {
            // LOGIN FAILED -> REGISTER NEW USER
            App.globalPlayer = new Player(username, "#00FFFF", 3.0, 1.5);
            App.globalPassword = password;
            
            // Save immediately to register them
            DataManager.savePlayer(App.globalPlayer, password);
            System.out.println("New User Identity Created.");
        }

        App.setRoot("MenuPage");
    }
}