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

        // 1. Check if we have text for this scene
        if (storyData.containsKey(sceneId)) {
            storyLabel.setText(storyData.get(sceneId));

            // 2. Try to load the image
            // Assuming images are in: src/main/resources/fop/assignment/images/
            String imagePath = "images/" + sceneId + ".png"; 
            
            try {
                // Debug print to help you find the error
                System.out.println("Attempting to load image: " + imagePath);
                
                String fullPath = getClass().getResource(imagePath).toExternalForm();
                cutsceneImage.setImage(new Image(fullPath));
                
            } catch (NullPointerException e) {
                System.out.println("ERROR: Image not found! Check if file exists: " + imagePath);
                cutsceneImage.setImage(null); // Clear previous image
            } catch (Exception e) {
                System.out.println("ERROR: Could not load image.");
                e.printStackTrace();
            }
        } else {
            // No more scenes in this chapter? Go back to Game.
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
        System.out.println("End of Chapter. Entering Game Arena...");
        App.setRoot("Arena"); // <--- CHANGE THIS to "Arena"
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