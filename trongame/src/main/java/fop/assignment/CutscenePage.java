package fop.assignment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;

public class CutscenePage {

    @FXML private Label titleLabel; // NEW: Needs to be added to FXML
    @FXML private Label storyLabel;
    @FXML private ImageView cutsceneImage;

    private Map<String, String> storyData = new HashMap<>();
    private String currentChapter; 
    private int currentSceneNumber = 1; 

    @FXML
    public void initialize() {
        loadStoryFromFile();
        this.currentChapter = App.currentChapterId; 
        this.currentSceneNumber = 1; 

        // --- NEW: Set the Chapter Title ---
        // Looks for "chapter1" in the text file
        if (storyData.containsKey(currentChapter)) {
            titleLabel.setText(storyData.get(currentChapter));
        } else {
            titleLabel.setText("Unknown Chapter");
        }

        loadCurrentScene();
        
        // Setup Keyboard Listeners (Enter/Space)
        if (storyLabel.getScene() != null) setupInput(storyLabel.getScene());
        else storyLabel.sceneProperty().addListener((obs, o, n) -> { if (n!=null) setupInput(n); });
    }

    private void setupInput(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                handleNext();
            }
        });
    }

    private void loadCurrentScene() {
        String sceneId = currentChapter + ".scene" + currentSceneNumber;

        if (storyData.containsKey(sceneId)) {
            storyLabel.setText(storyData.get(sceneId));

            // Load Image (e.g., chapter1.scene1.png)
            String imagePath = "/fop/assignment/images/" + sceneId + ".png";
            try {
                cutsceneImage.setImage(new Image(getClass().getResourceAsStream(imagePath)));
            } catch (Exception e) {
                // Keep previous image or show nothing if missing
            }
        } else {
            // No more scenes? Go to Game.
            enterGame();
        }
    }

    @FXML
    private void handleNext() {
        currentSceneNumber++; 
        loadCurrentScene();
    }

    private void enterGame() {
        try {
            App.setRoot("GamePage"); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadStoryFromFile() {
        try {
            File file = new File("story.txt"); 
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                // Split only on the FIRST colon found
                String[] parts = line.split(":", 2);
                if (parts.length >= 2) {
                    storyData.put(parts[0].trim(), parts[1].trim());
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            storyLabel.setText("story.txt not found in project root.");
        }
    }
}