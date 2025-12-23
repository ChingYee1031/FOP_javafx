package fop.assignment;

public class Player extends GameCharacter {
    
    // New fields for progression
    private int xp = 0;
    private int level = 1;
    private final int MAX_LEVEL = 99; // Assignment Requirement: Max level 99 [cite: 98]

    public Player(String name, String color, double lives, double speed) {
        super(name, color, lives, speed); // Pass data to the parent class
    }

    // Method to add XP when an enemy is defeated
    public void addXP(int amount) {
        this.xp += amount;
        
        // Check if we have enough XP to level up
        // Simple formula: First 10 levels need less XP 
        int xpNeeded = (level <= 10) ? level * 100 : level * 300; 
        
        while (this.xp >= xpNeeded && level < MAX_LEVEL) {
            this.xp -= xpNeeded; // Carry over extra XP
            levelUp();
            // Recalculate requirement for the next level
            xpNeeded = (level <= 10) ? level * 100 : level * 300;
        }
    }

    // Assignment Requirement 2.2.1: Method called levelUp() 
    public void levelUp() {
        level++;
        
        // Requirement: +1 life every 10 levels [cite: 95]
        if (level % 10 == 0) {
            this.lives += 1.0;
        }

        // Custom leveling algorithm based on character type [cite: 99, 102]
        if (this.name.equalsIgnoreCase("Kevin")) {
            // Kevin gains more handling (not implemented yet) or efficiency
            // For now, we just give a small speed boost
            this.speed += 0.3; 
        } else {
            // Tron (Default) gains more speed [cite: 100]
            this.speed += 0.5;
        }
    }

    // --- Getters for the HUD to display ---
    public int getXP() { return xp; }
    public int getLevel() { return level; }
    public String getName() { return name; }
}