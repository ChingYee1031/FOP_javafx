package fop.assignment;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class CharacterSelection {

    @FXML private Label tronNameLabel;
    @FXML private Button selectTronButton;

    @FXML private Label kevinNameLabel;
    @FXML private Button selectKevinButton;
    @FXML private Label lockMessage; 

    @FXML
    public void initialize() {
        if (App.globalPlayer != null) {
            int level = App.globalPlayer.getLevel();
            if (level < 10) {
                selectKevinButton.setDisable(true);
                kevinNameLabel.setText("LOCKED (Lv 10)");
                if (lockMessage != null) lockMessage.setText("Reach Level 10 to unlock Kevin!");
            } else {
                selectKevinButton.setDisable(false);
                kevinNameLabel.setText("Kevin Flynn");
                if (lockMessage != null) lockMessage.setText("Character Unlocked!");
            }
        }
    }

    @FXML
    private void handleSelectTron() throws IOException {
        System.out.println("Tron Selected");
        if (App.globalPlayer != null) {
            App.globalPlayer.setColor("#0091ffff"); 
            App.globalPlayer.setSpeed(1.5); 
            App.globalPlayer.setCharacterModel("Tron");
        } else {
            App.globalPlayer = new Player("Tron", "#0091ffff", 3.0, 1.5);
        }
        startGame();
    }

    @FXML
    private void handleSelectKevin() throws IOException {
        System.out.println("Kevin Selected");
        if (App.globalPlayer != null) {
            App.globalPlayer.setColor("#ffffffff"); 
            App.globalPlayer.setSpeed(1.5); 
            App.globalPlayer.setCharacterModel("Kevin");
        } else {
            App.globalPlayer = new Player("Kevin", "#ffffffff", 3.0, 1.5);
        }
        startGame();
    }

    private void startGame() throws IOException {
        // 1. Tutorial Check
        if (App.globalPlayer != null && !App.globalPlayer.hasSeenTutorial()) {
            System.out.println("First time player -> Going to Tutorial Page");
            App.setRoot("TutorialPage");
            return; 
        }

        // 2. Story vs Arena Check (Level 1 goes to Story)
        if (App.globalPlayer.getLevel() == 1) {
            System.out.println("New Game detected. Playing Intro Cutscene...");
            App.goToCutscene("chapter1"); 
        } else {
            System.out.println("Returning Player. Loading Arena directly...");
            App.setRoot("Arena"); 
        }
    }
}