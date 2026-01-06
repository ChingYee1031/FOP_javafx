package fop.assignment;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static Scene scene;
    
    // --- GLOBAL STATE (Saves progress between screens) ---
    public static Player globalPlayer; 
    public static CharacterSelection globalSelectedCharacter;
    public static String globalPassword;
    
    // Tracks which chapter to show next (Default: chapter1)
    public static String currentChapterId = "chapter1"; 

    @Override
    public void start(Stage stage) throws IOException {
        // Start at the Start Page
        scene = new Scene(loadFXML("StartPage"), 800, 600); // 800x600 default size
        stage.setScene(scene);
        stage.show();

        // --- NEW: AUTO-SAVE ON EXIT ---
        // This listens for the "Red X" close button click
        stage.setOnCloseRequest(event -> {
            System.out.println("Window closing... Checking for unsaved progress.");
            
            // Only save if a player is actually logged in
            if (globalPlayer != null && globalPassword != null) {
                DataManager.savePlayer(globalPlayer, globalPassword);
                System.out.println("Progress Auto-Saved on Exit!");
            }
            
            Platform.exit();
            System.exit(0);
        });
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
        
        // --- AUTO-RESIZE WINDOW ---
        // Ensures the window resizes to fit larger screens (like the Arena)
        if (scene.getWindow() != null) {
            Stage stage = (Stage) scene.getWindow();
            stage.sizeToScene(); 
            stage.centerOnScreen(); // Keeps the window centered on your monitor
        }
    }

    // Helper to switch to a specific cutscene
    public static void goToCutscene(String chapterId) throws IOException {
        currentChapterId = chapterId;
        setRoot("CutscenePage");
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}