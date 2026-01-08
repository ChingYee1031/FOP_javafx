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

    @FXML private Label titleLabel; 
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

        // Set the Chapter Title
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

            // 2. Load the image from Project Folder (Standard File logic)
            String imagePath = "images/" + sceneId + ".png"; 
            
            try {
                File file = new File(imagePath);
                
                // Debugging print
                System.out.println("Loading Cutscene Image: " + file.getAbsolutePath());

                if (file.exists()) {
                    // Load using file URI (Fixes path issues)
                    cutsceneImage.setImage(new Image(file.toURI().toString()));
                } else {
                    System.out.println("ERROR: Image file missing at: " + imagePath);
                    cutsceneImage.setImage(null); 
                }
            } catch (Exception e) {
                System.out.println("ERROR: Could not load image.");
                e.printStackTrace();
            }
        } else {
            // No more scenes? Decide where to go next.
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
            // Chapter 10 sends player back to Arena to fight the final battle
            System.out.println("End of Cutscene. Entering Game Arena...");
            App.setRoot("Arena");
            
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