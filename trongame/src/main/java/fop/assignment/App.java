package fop.assignment;

import java.io.IOException;

import javafx.application.Application;
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
        // Start at the Character Selection or Start Page
        scene = new Scene(loadFXML("StartPage"), 1280, 720); // 800x600 for better view
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
        
        // --- NEW: AUTO-RESIZE WINDOW ---
        // This gets the window (Stage) and tells it to snap to the new content's size
        if (scene.getWindow() != null) {
            Stage stage = (Stage) scene.getWindow();
            stage.sizeToScene(); 
            stage.centerOnScreen(); // Optional: Keeps the window centered
        }
    }

    // NEW: Helper to switch to a specific cutscene
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