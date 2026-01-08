package fop.assignment;

public class Player extends GameCharacter {
    
    private int xp = 0;
    private int level = 1;
    private final int MAX_LEVEL = 99; 
    private boolean seenTutorial = false; 

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

    public void addXP(int amount) {
        this.xp += amount;
        
        // Level Up Threshold
        int xpNeeded = (level <= 10) ? level * 100 : level * 300; 
        
        while (this.xp >= xpNeeded && level < MAX_LEVEL) {
            this.xp -= xpNeeded; 
            levelUp();
            xpNeeded = (level <= 10) ? level * 100 : level * 300;
        }
    }

    public void levelUp() {
        level++;
        
        // Requirement: +1 Life every 10 levels
        if (level % 10 == 0) {
            this.lives += 1.0;
        }

        // --- REQUIREMENT: +1 Disc Slot every 15 Levels ---
        if (level % 15 == 0) {
            this.maxDiscSlots++;
            this.currentDiscSlots++; // Fill the new slot immediately
            System.out.println("UPGRADE: Max Disc Slots increased to " + maxDiscSlots);
        }

        // Stat Boosts
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