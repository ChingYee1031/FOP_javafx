package fop.assignment;

import java.io.File;
import java.util.Scanner;

/**
 * This is the base class (Model). 
 * It defines what every character "is" and "can do".
 */
public abstract class Character {
    // Attributes shared by all characters
    protected String name;
    protected String color;
    protected String handling;
    protected double speed;
    protected double lives;
    protected int experiencePoints;

    // --- Getters ---
    // These allow the ArenaController to access the private/protected data
    public String getName() { return name; }
    public String getColor() { return color; }
    public double getSpeed() { return speed; }
    public double getLives() { return lives; }

    /**
     * Loads attributes from characters.txt based on the character's name.
     * This logic is now centralized so any subclass (Tron/Kevin) can use it.
     */
    public void loadAttributes(String targetName) {
        // Default fallback color (Lime Green)
        this.color = "#00FF00"; 

        try {
            File file = new File("characters.txt");
            if (!file.exists()) {
                System.err.println("CRITICAL: characters.txt missing from " + file.getAbsolutePath());
                return;
            }

            Scanner reader = new Scanner(file);
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                String[] data = line.split(",");

                // data[0] is Name, data[1] is Color, data[2] is Speed, data[4] is Lives
                if (data.length > 4 && data[0].equalsIgnoreCase(targetName)) {
                    this.name = data[0];
                    
                    // Map color names to Hex codes
                    if (data[1].equalsIgnoreCase("Blue")) {
                        this.color = "#0000FF";
                    } else if (data[1].equalsIgnoreCase("White")) {
                        this.color = "#FFFFFF";
                    } else if (data[1].equalsIgnoreCase("Cyan")) {
                        this.color = "#00FFFF";
                    }

                    // Map Speed: "High" sets speed to 2.5, otherwise 1.5
                    this.speed = data[2].contains("High") ? 2.5 : 1.5;
                    
                    // Parse lives
                    this.lives = Double.parseDouble(data[4]);
                    
                    break; // Found the character, stop searching
                }
            }
            reader.close();
        } catch (Exception e) {
            System.err.println("Error loading character attributes: " + e.getMessage());
        }
    }
}