package fop.assignment;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {
    public static Character chosenCharacter;
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("StartPage"), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    //username
    public static String currentPlayerName = "";

    //hold current selected character object
    public static CharacterSelection globalSelectedCharacter;

    //story progress
    /*public class App extends Application {
        public static Map<String, String> storyData = new HashMap<>();
        public static String targetScene = "";
        public static int targetLevel = 1;

        @Override
        public void start(Stage stage) throws IOException {
            loadStoryKeys();

            scene = new Scene(loadFXML("MainMenu"),640,480);
            stage.setScene(scene);
            stage.show();
        }

        private void loadStoryKeys() {
           try{
            File file = new File("story.txt");
            if(file.exists()){
                Scanner scanner = new Scanner(file);
                while(scanner.hasNextLine()){
                    String line = scanner.nextLine().trim();
                    if(line.isEmpty() || line.startsWith("#")) continue;

                    String[] parts = line.split(":",2);
                    if(parts.length >= 1){
                        storyData.put(parts[0].trim(), parts.length > 1 ? parts[1].trim() : "");
                    }
                }
                scanner.close();
            }
        }catch (Exception e){
               e.printStackTrace();
               System.out.println("Error loading story keys.");
           }
        }
    }
        */

    public static void main(String[] args) {
        launch();
    }

}