package fop.assignment;

import java.net.URL;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class SoundManager {

    private static MediaPlayer backgroundPlayer;
    private static Timeline fadeTimeline; // <--- NEW: To control the fade animation

    public static void playSound(String fileName) {
        try {
            // 1. Attempt to find the file
            URL resource = SoundManager.class.getResource("sounds/" + fileName);
            
            // 2. SAFETY CHECK
            if (resource == null) {
                System.out.println("⚠️ Warning: Sound file not found: " + fileName);
                return; 
            }

            // 3. Play the sound
            String path = resource.toExternalForm();
            AudioClip clip = new AudioClip(path);
            clip.play();
            
        } catch (Exception e) {
            System.out.println("Error playing sound: " + fileName);
        }
    }

    public static void playMusic(String fileName) {
        try {
            // 1. Stop existing music AND any running fade animations
            if (backgroundPlayer != null) {
                backgroundPlayer.stop();
            }
            if (fadeTimeline != null) {
                fadeTimeline.stop();
            }
            
            // 2. Check File Existence
            URL resource = SoundManager.class.getResource("sounds/" + fileName);
            if (resource == null) {
                System.out.println("⚠️ Warning: Music file not found: " + fileName);
                return;
            }

            String path = resource.toExternalForm();
            Media media = new Media(path);
            backgroundPlayer = new MediaPlayer(media);
            backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE); 

            // 3. Determine the Target Volume (Goal)
            double targetVolume = 0.5; // Default (Normal)

            if (fileName.equalsIgnoreCase("cutscenebgm.wav")) {
                targetVolume = 0.25; // Goal is 25%
            } 
            else if(fileName.equalsIgnoreCase("endingbgm.mp3")) {
                targetVolume = 0.3; // Goal is 30%
            }
            else if(fileName.equalsIgnoreCase("gameover.wav")) {
                targetVolume = 0.3; // Goal is 30%
            }

            // 4. Play with Fade-In OR Normal Start
            if (fileName.equalsIgnoreCase("cutscenebgm.wav")) {
                // --- FADE IN LOGIC ---
                backgroundPlayer.setVolume(0); // Start silent
                backgroundPlayer.play();

                // Animate Volume from 0 -> targetVolume over 1.0 second
                fadeTimeline = new Timeline(
                    new KeyFrame(Duration.seconds(1.0), 
                    new KeyValue(backgroundPlayer.volumeProperty(), targetVolume))
                );
                fadeTimeline.play();
            } else {
                // --- NORMAL LOGIC ---
                backgroundPlayer.setVolume(targetVolume);
                backgroundPlayer.play();
            }
            
        } catch (Exception e) {
            System.out.println("Error playing music: " + fileName);
        }
    }

    public static void stopMusic() {
        // Stop the fade animation if it's running
        if (fadeTimeline != null) {
            fadeTimeline.stop();
        }
        // Stop the music
        if (backgroundPlayer != null) {
            backgroundPlayer.stop();
        }
    }
    
    public static void pauseMusic() {
        if (backgroundPlayer != null) backgroundPlayer.pause();
    }

    public static void resumeMusic() {
        if (backgroundPlayer != null) backgroundPlayer.play();
    }
}