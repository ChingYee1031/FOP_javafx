package fop.assignment;

import java.net.URL;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SoundManager {

    private static MediaPlayer backgroundPlayer;

    public static void playSound(String fileName) {
        try {
            // 1. Attempt to find the file
            URL resource = SoundManager.class.getResource("sounds/" + fileName);
            
            // 2. SAFETY CHECK: If file is missing, do not crash!
            if (resource == null) {
                System.out.println("⚠️ Warning: Sound file not found: " + fileName);
                return; // Just exit the method, allowing the game to continue
            }

            // 3. Play the sound
            String path = resource.toExternalForm();
            AudioClip clip = new AudioClip(path);
            clip.play();
            
        } catch (Exception e) {
            // If any other error happens, print it but don't crash
            System.out.println("Error playing sound: " + fileName);
        }
    }

    public static void playMusic(String fileName) {
        try {
            if (backgroundPlayer != null) {
                backgroundPlayer.stop();
            }
            
            URL resource = SoundManager.class.getResource("sounds/" + fileName);
            if (resource == null) {
                System.out.println("⚠️ Warning: Music file not found: " + fileName);
                return;
            }

            String path = resource.toExternalForm();
            Media media = new Media(path);
            backgroundPlayer = new MediaPlayer(media);
            backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE); 
            if (fileName.equalsIgnoreCase("cutscenebgm.wav")) {
                backgroundPlayer.setVolume(0.2); // 20% Volume (Quiet)
            } 
            else if(fileName.equalsIgnoreCase("endingbgm.mp3")) {
                backgroundPlayer.setVolume(0.3); // 30% Volume (Moderate)
            }
            else {
                backgroundPlayer.setVolume(0.5); // 50% Volume (Normal)
            }
            backgroundPlayer.play();
            
        } catch (Exception e) {
            System.out.println("Error playing music: " + fileName);
        }
    }

    public static void stopMusic() {
        if (backgroundPlayer != null) backgroundPlayer.stop();
    }
    
    public static void pauseMusic() {
        if (backgroundPlayer != null) backgroundPlayer.pause();
    }

    public static void resumeMusic() {
        if (backgroundPlayer != null) backgroundPlayer.play();
    }
}