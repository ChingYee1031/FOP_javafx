package fop.assignment;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;

public class TutorialPage {

    @FXML private StackPane rootPane;

    @FXML
    public void initialize() {
        // Wait for the scene to load, then attach the Key Listener
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.SPACE) {
                        handleStartGame();
                    }
                });
            }
        });
        
        // Ensure this screen has focus so it catches the key press
        rootPane.setFocusTraversable(true);
        javafx.application.Platform.runLater(() -> rootPane.requestFocus());
    }

    private void handleStartGame() {
        try {
            // 1. Mark Tutorial as Seen & Save
            if (App.globalPlayer != null) {
                App.globalPlayer.setSeenTutorial(true);
                DataManager.savePlayer(App.globalPlayer, App.globalPassword);
            }

            // 2. Start the Game (Or Cutscene if you have one)
            // If you want the story to start first, change "Arena" to "CutscenePage"
            App.setRoot("Arena"); 
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}