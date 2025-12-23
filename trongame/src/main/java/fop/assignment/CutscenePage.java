package fop.assignment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class CutscenePage {

    @FXML
    private Label storyLabel;

    @FXML
    private ImageView cutsceneImage;

    // Data Storage
    private Map<String, String> storyData = new HashMap<>();

    // State Tracking
    private String currentChapter = "chapter1"; // Default to Chapter 1
    private int currentSceneNumber = 1;         // Start at Scene 1

    @FXML
    public void initialize() {
        // 1. Load all text from the file into memory
        loadStoryFromFile();

        // 2. Check if a specific chapter was requested globally (Optional)
        // if (App.targetChapter != null) currentChapter = App.targetChapter;

        // 3. Display the first scene (e.g., "chapter1.scene1")
        loadCurrentScene();
    }

    private void loadCurrentScene() {
        // Construct the ID: "chapter1" + ".scene" + "1" = "chapter1.scene1"
        String sceneId = currentChapter + ".scene" + currentSceneNumber;

        // Check if this scene actually exists in our Map
        if (storyData.containsKey(sceneId)) {
            // A. Update Text
            storyLabel.setText(storyData.get(sceneId));

            // B. Update Image
            // Looks for: images/chapter1.scene1.png
            String imagePath = "images/" + sceneId + ".png";
            File imageFile = new File(imagePath);

            if (imageFile.exists()) {
                cutsceneImage.setImage(new Image(imageFile.toURI().toString()));
            } else {
                // If image is missing, maybe keep the old one or clear it
                System.out.println("Image missing: " + imagePath);
            }
        } else {
            // If the scene key doesn't exist (e.g., "chapter1.scene5"), 
            // it means the chapter is over!
            enterGame();
        }
    }

    @FXML
    private void handleNext() {
        // Increment the counter to the next scene (1 -> 2 -> 3)
        currentSceneNumber++; 
        
        // Try to load it. 
        // logic inside loadCurrentScene() will decide if we continue or end.
        loadCurrentScene();
    }

    private void enterGame() {
        try {
            System.out.println("End of Chapter. Entering Game Arena...");
            App.setRoot("GameArena");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadStoryFromFile() {
        try {
            File file = new File("story.txt");
            System.out.println("Looking for story at: " + file.getAbsolutePath()); // DEBUG PRINT

            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                
                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(":", 2);
                if (parts.length >= 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    storyData.put(key, value);
                    System.out.println("Loaded Key: " + key); // DEBUG PRINT
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: story.txt NOT FOUND!");
            storyLabel.setText("Error: story.txt not found.");
        }
    }
}