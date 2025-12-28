package fop.assignment;

public class Player extends GameCharacter {
    
    private int xp = 0;
    private int level = 1;
    private final int MAX_LEVEL = 99; 

    public Player(String name, String color, double lives, double speed) {
        super(name, color, lives, speed);
        
        // --- DISC SLOT SETUP ---
        if (name.equalsIgnoreCase("Kevin")) {
            this.maxDiscSlots = 5; // Kevin starts with 5 slots
        } else {
            this.maxDiscSlots = 3; // Tron starts with 3 slots
        }
        this.currentDiscSlots = this.maxDiscSlots;
    }

    public void addXP(int amount) {
        this.xp += amount;
        
        // Simple Level Up Formula
        int xpNeeded = (level <= 10) ? level * 100 : level * 300; 
        
        while (this.xp >= xpNeeded && level < MAX_LEVEL) {
            this.xp -= xpNeeded; 
            levelUp();
            xpNeeded = (level <= 10) ? level * 100 : level * 300;
        }
    }


    public void levelUp() {
        level++;
        
        // --- REQUIREMENT: +1 life every 10 levels ---
        if (level % 10 == 0) {
            this.lives += 1.0;
            System.out.println("BONUS: +1 Life! Current: " + this.lives);
        }

        // --- REQUIREMENT: +1 Disc Slot every 15 Levels ---
        if (level % 15 == 0) {
            this.maxDiscSlots++;
            this.currentDiscSlots++; 
            System.out.println("UPGRADE: Disc Slots Increased to " + maxDiscSlots);
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
}