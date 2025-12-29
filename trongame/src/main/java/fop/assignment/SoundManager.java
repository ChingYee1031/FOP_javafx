package fop.assignment;

// These imports are crucial. If they are red, check pom.xml and reload Maven.
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SoundManager {

    private static MediaPlayer backgroundPlayer;

    // Plays short sound effects (e.g., Shoot, Hit, Game Over)
    public static void playSound(String fileName) {
        try {
            // Looks in: src/main/resources/fop/assignment/sounds/
            String path = SoundManager.class.getResource("sounds/" + fileName).toExternalForm();
            AudioClip clip = new AudioClip(path);
            clip.play();
        } catch (Exception e) {
            System.out.println("Error playing sound: " + fileName);
            e.printStackTrace(); 
        }
    }

    // Plays long background music (loops forever)
    public static void playMusic(String fileName) {
        try {
            if (backgroundPlayer != null) {
                backgroundPlayer.stop();
            }
            String path = SoundManager.class.getResource("sounds/" + fileName).toExternalForm();
            Media media = new Media(path);
            backgroundPlayer = new MediaPlayer(media);
            backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop forever
            backgroundPlayer.setVolume(0.5); // 50% volume
            backgroundPlayer.play();
        } catch (Exception e) {
            System.out.println("Error playing music: " + fileName);
        }
    }

    public static void stopMusic() {
        if (backgroundPlayer != null) backgroundPlayer.stop();
    }
}
