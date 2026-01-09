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
        
        // --- 1. STOP IF ALREADY MAX LEVEL ---
        if (level >= MAX_LEVEL) {
            return; 
        }

        while (true) {
            // --- 2. STOP IF WE HIT MAX LEVEL DURING LOOP ---
            if (level >= MAX_LEVEL) {
                break; 
            }

            int xpNeeded;

            // --- PHASE 1: KOURA (Levels 1-4) ---
            if (this.level < 5) {
                xpNeeded = this.level * 20;
            } 
            // --- PHASE 2: SARK (Levels 5-9) ---
            else if (this.level < 10) {
                xpNeeded = this.level * 120;
            }
            // --- PHASE 3: THE CLIMB (Levels 10-29) ---
            // Difficulty rises quickly here (6 kills -> 9 kills)
            else if (this.level < 30) {
                xpNeeded = this.level * 300;
            }
            // --- PHASE 4: THE GRIND (Levels 30-98) ---
            // Difficulty flattens out. 
            // Starts at 9,000. Ends at ~17,100.
            else {
                xpNeeded = 9000 + ((this.level - 30) * 120);
            }

            // If we have enough XP, level up and subtract the cost
            if (this.xp >= xpNeeded) {
                this.xp -= xpNeeded;
                levelUp();
            } else {
                break;
            }
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