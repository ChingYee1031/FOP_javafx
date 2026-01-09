package fop.assignment;

public class Player extends GameCharacter {

    
    private int xp = 0;
    private int level = 1;
    private final int MAX_LEVEL = 99; 
    private boolean seenTutorial = false; 
    private String characterModel = "Tron"; // default to tron

    public Player(String name, String color, double lives, double speed) {
        super(name, color, lives, speed);
        
        // Initial Slots based on Character
        if (name.equalsIgnoreCase("Kevin")) {
            this.maxDiscSlots = 5; 
        } else {
            this.maxDiscSlots = 3; 
        }
        this.currentDiscSlots = this.maxDiscSlots;
    }

    // --- NEW: Getter and Setter for the Hero Name ---
    public String getCharacterModel() { return characterModel; }
    public void setCharacterModel(String model) { this.characterModel = model; }

    public void addXP(int amount) {
        this.xp += amount;
        
        // Use 20 scaling factor to match the 10 XP enemies
        int xpNeeded = level * 20; 
        
        while (this.xp >= xpNeeded) {
            this.xp -= xpNeeded;
            levelUp();
            xpNeeded = level * 20; 
        }
    }

    public void levelUp() {
        level++;
        
        // --- NEW: RESTORE HEALTH TO FULL ---
        // 1. Calculate Max Life: Base is 3.0. You get +1 Max Life for every 10 levels.
        double maxLives = 3.0 + (int)(level / 10);
        
        // 2. Set current lives to that maximum
        this.lives = maxLives;
        
        // --- DISC SLOT UPGRADES ---
        // +1 Disc Slot every 15 Levels
        if (level % 15 == 0) {
            this.maxDiscSlots++;
            this.currentDiscSlots++; // Fill the new slot immediately
            System.out.println("UPGRADE: Max Disc Slots increased to " + maxDiscSlots);
        }

        // --- STAT BOOSTS ---
        if (this.name.equalsIgnoreCase("Kevin")) {
            this.speed += 0.3; 
        } else {
            this.speed += 0.5;
        }
    }

    // --- GETTERS & SETTERS ---
    public int getXP() { return xp; }
    public int getLevel() { return level; }
    
    public void setLevel(int level) { this.level = level; }
    public void setXP(int xp) { this.xp = xp; }
    
    public void setLives(double lives) { this.lives = lives; }
    public void setName(String name) { this.name = name; }
    public void setColor(String color) { this.color = color; }
    public void setSpeed(double speed) { this.speed = speed; }

    public boolean hasSeenTutorial() { return seenTutorial; }
    public void setSeenTutorial(boolean seen) { this.seenTutorial = seen; }
}