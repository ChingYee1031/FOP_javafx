package fop.assignment;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;

public class TutorialPage {

    @FXML private StackPane rootPane;

    @FXML
    public void initialize() {
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.SPACE) {
                        handleFinishTutorial();
                    }
                });
            }
        });
        
        rootPane.setFocusTraversable(true);
        javafx.application.Platform.runLater(() -> rootPane.requestFocus());
    }

    private void handleFinishTutorial() {
        try {
            // 1. Mark Tutorial as Seen & Save
            if (App.globalPlayer != null) {
                App.globalPlayer.setSeenTutorial(true);
                DataManager.savePlayer(App.globalPlayer, App.globalPassword);
                System.out.println("Tutorial Complete. Flag Saved.");
            }

            // 2. Decide Next Step
            if (App.globalPlayer.getLevel() == 1) {
                System.out.println("Level 1 detected -> Going to Chapter 1 Cutscene");
                App.goToCutscene("chapter1");
            } 
            else {
                System.out.println("Level > 1 detected -> Going to Arena");
                App.setRoot("Arena");
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}