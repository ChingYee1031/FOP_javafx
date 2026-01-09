package fop.assignment;

import java.io.File;
import java.util.Scanner;

public abstract class GameCharacter {
    
    // --- BASIC ATTRIBUTES (From old Character.java) ---
    protected String name;
    protected String color;
    protected double lives;
    protected double speed;
    
    // --- POSITIONING (From old GameCharacter.java) ---
    protected int x, y;
    
    // --- DISC/COMBAT MECHANICS (From old GameCharacter.java) ---
    protected int maxDiscSlots = 1; 
    protected int currentDiscSlots = 1;
    protected long lastThrowTime = 0;
    protected static final long COOLDOWN_MS = 5000; // 5 Seconds

    // --- CONSTRUCTOR ---
    public GameCharacter(String name, String color, double lives, double speed) {
        this.name = name;
        this.color = color;
        this.lives = lives;
        this.speed = speed;
        
        // Default Disc Setup
        this.maxDiscSlots = 1;
        this.currentDiscSlots = 1;
        
        // OPTIONAL: Immediately try to load better stats from file if they exist
        loadAttributesFromFile(name);
    }

    // --- DATA LOADING (Moved from Character.java) ---
    /**
     * Looks for the character in characters.txt and overrides stats if found.
     */
    public void loadAttributesFromFile(String targetName) {
        File file = new File("characters.txt");
        
        // If file doesn't exist, just keep the default values passed in constructor
        if (!file.exists()) return; 

        try (Scanner reader = new Scanner(file)) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                String[] data = line.split(",");

                // Expected format: Name, ColorName, Handling, SpeedDesc, Lives
                if (data.length > 4 && data[0].equalsIgnoreCase(targetName)) {
                    // 1. Update Name
                    this.name = data[0];
                    
                    // 2. Map Color Names to Hex
                    if (data[1].equalsIgnoreCase("Blue")) this.color = "#0000FF";
                    else if (data[1].equalsIgnoreCase("White")) this.color = "#FFFFFF";
                    else if (data[1].equalsIgnoreCase("Cyan")) this.color = "#00FFFF";
                    else if (data[1].equalsIgnoreCase("Red")) this.color = "#FF0000";
                    else if (data[1].equalsIgnoreCase("Yellow")) this.color = "#FFFF00";
                    else if (data[1].equalsIgnoreCase("Green")) this.color = "#00FF00";

                    // 3. Map Speed
                    // If text contains "High", set 2.5, else 1.5 (or keep current)
                    this.speed = data[2].contains("High") ? 2.5 : 1.5;
                    
                    // 4. Map Lives
                    this.lives = Double.parseDouble(data[4]);
                    
                    // Debug print to confirm loading happened
                    System.out.println("Loaded stats for " + this.name + ": Speed=" + this.speed + ", Lives=" + this.lives);
                    break; 
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading character file: " + e.getMessage());
        }
    }

    // --- MOVEMENT ---
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    // --- COMBAT & STATS ---
    public void reduceLives(double amount) {
        this.lives -= amount;
        if (this.lives < 0) this.lives = 0;
    }

    public boolean isAlive() {
        return lives > 0;
    }

    // --- DISC MECHANICS ---
    public boolean hasDisc() { 
        return currentDiscSlots > 0;
    }

    public void useDisc() { 
        if (currentDiscSlots > 0) currentDiscSlots--;
    }

    public void recoverDisc() {
        if (currentDiscSlots < maxDiscSlots) {
            currentDiscSlots++;
            this.lastThrowTime = 0; // Reset cooldown on catch
        }
    }

    public void refillDiscSlots() {
        this.currentDiscSlots = this.maxDiscSlots;
        this.lastThrowTime = 0; 
    }

    // --- COOLDOWN LOGIC ---
    public boolean isCooldownReady() {
        return System.currentTimeMillis() - lastThrowTime >= COOLDOWN_MS;
    }
    
    public void resetCooldown() {
        lastThrowTime = System.currentTimeMillis();
    }
    
    public long getCooldownRemaining() {
        long diff = COOLDOWN_MS - (System.currentTimeMillis() - lastThrowTime);
        return (diff < 0) ? 0 : diff;
    }

    // --- GETTERS ---
    public int getCurrentDiscSlots() { return currentDiscSlots; }
    public int getMaxDiscSlots() { return maxDiscSlots; }
    
    public String getName() { return name; }
    public String getColor() { return color; }
    public double getLives() { return lives; }
    public double getSpeed() { return speed; }
    
    // Setters needed for upgrades (e.g., Level Up in Player class)
    public void setLives(double lives) { this.lives = lives; }
    public void setSpeed(double speed) { this.speed = speed; }
    public void setColor(String color) { this.color = color; }
}