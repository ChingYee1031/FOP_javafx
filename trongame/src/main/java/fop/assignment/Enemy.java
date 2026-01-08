package fop.assignment;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import javafx.scene.image.Image;

public class Enemy extends GameCharacter {

    private String difficulty; 
    private int xpReward;
    private String intelligence; 
    
    private Image enemyIcon;
    private int currentDir = -1; 

    // --- NEW MOVEMENT VARIABLES ---
    private int stepsRemainingInCurrentDir = 0; // How many steps left to walk in same dir
    private int minStraightSteps; // Minimum steps to walk straight
    private int maxStraightSteps; // Maximum steps to walk straight

    // Shooting
    private long lastShotTime = 0;
    private static final long SHOOT_DELAY = 2_000_000_000L; 

    public Enemy(String name, String color, double lives, double speed, String difficulty, int xpReward, String intelligence) {
        // We pass 'speed' to super, but we will overwrite it in configureEnemyStats based on the name
        super(name, color, lives, speed);
        this.difficulty = difficulty;
        this.xpReward = xpReward;
        this.intelligence = intelligence;
        
        // Ammo Setup
        if (difficulty.equalsIgnoreCase("Impossible")) {
            this.maxDiscSlots = 3;
        } else if (difficulty.equalsIgnoreCase("Hard")) {
            this.maxDiscSlots = 2;
        } else {
            this.maxDiscSlots = 1;
        }
        this.currentDiscSlots = this.maxDiscSlots;
        
        loadEnemyImage();
        
        // Apply specific stats based on the specific Enemy Name
        configureEnemyStats();
    }

    private void configureEnemyStats() {
        // Default settings
        this.minStraightSteps = 1;
        this.maxStraightSteps = 5;

        // Customize based on name hierarchy
        switch (this.name) {
            case "Koura": // EASIEST
                this.speed = 1.0; // Slowest
                // Moves in long straight lines (very predictable)
                this.minStraightSteps = 6;
                this.maxStraightSteps = 12; 
                break;

            case "Sark": // MEDIUM
                this.speed = 1.3; // Slightly faster
                // Moderate straight lines
                this.minStraightSteps = 4;
                this.maxStraightSteps = 8;
                break;

            case "Rinzler": // HARD
                this.speed = 1.6; // Fast
                // Shorter lines, changes direction more often
                this.minStraightSteps = 2;
                this.maxStraightSteps = 6;
                break;

            case "Clu": // HARDEST
                this.speed = 1.9; // Very Fast (but playable)
                // Very erratic, changes direction constantly
                this.minStraightSteps = 1;
                this.maxStraightSteps = 3;
                break;
                
            default:
                // Fallback for custom enemies
                this.speed = 1.2;
                this.minStraightSteps = 3;
                this.maxStraightSteps = 7;
                break;
        }
    }

    private void loadEnemyImage() {
        try {
            String path = "images/" + this.name + ".png";
            File file = new File(path);
            if (file.exists()) {
                this.enemyIcon = new Image(file.toURI().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean canShoot() {
        if (!hasAmmo()) return false;
        long now = System.nanoTime();
        if (now - lastShotTime > SHOOT_DELAY) {
            lastShotTime = now;
            return true;
        }
        return false;
    }

    public Image getIcon() { return this.enemyIcon; }
    public int getXPReward() { return this.xpReward; }

    // --- NEW AI MOVEMENT LOGIC ---
    public int makeMove(int[][] grid) {
        if (!isAlive()) return -1;

        // 1. Check if we should continue moving in the current locked direction
        if (currentDir != -1 && stepsRemainingInCurrentDir > 0) {
            // Calculate where the current direction leads
            int nextX = x;
            int nextY = y;

            if (currentDir == 0) nextY--;      // UP
            else if (currentDir == 1) nextY++; // DOWN
            else if (currentDir == 2) nextX--; // LEFT
            else if (currentDir == 3) nextX++; // RIGHT

            // Check if continuing is safe
            if (isSafe(nextX, nextY, grid)) {
                stepsRemainingInCurrentDir--; // Decrease counter
                return currentDir; // Continue same way
            } else {
                // We hit a wall or trail! Stop "locking" this direction immediately.
                stepsRemainingInCurrentDir = 0;
            }
        }

        // 2. If we are here, we need to pick a NEW direction (either counter expired or hit wall)
        ArrayList<Integer> validMoves = new ArrayList<>();
        // 0=UP, 1=DOWN, 2=LEFT, 3=RIGHT
        if (isSafe(x, y - 1, grid)) validMoves.add(0); 
        if (isSafe(x, y + 1, grid)) validMoves.add(1); 
        if (isSafe(x - 1, y, grid)) validMoves.add(2); 
        if (isSafe(x + 1, y, grid)) validMoves.add(3);

        if (validMoves.isEmpty()) return -1; 

        Random rand = new Random();
        
        // Pick a random valid move
        int chosenMove = validMoves.get(rand.nextInt(validMoves.size()));

        // 3. Set the new Direction and the new "Lock" duration
        this.currentDir = chosenMove;
        
        // Generate a random number between min and max steps
        this.stepsRemainingInCurrentDir = rand.nextInt((maxStraightSteps - minStraightSteps) + 1) + minStraightSteps;

        return chosenMove;
    }

    // --- CRITICAL FIX HERE ---
    private boolean isSafe(int tx, int ty, int[][] grid) {
        // 1. Bounds Check
        if (tx < 0 || tx >= 40 || ty < 0 || ty >= 40) return false;
        
        // 2. Obstacle Check
        // 1 = Wall, 2 = Player Trail, 4 = Enemy Trail
        int cell = grid[tx][ty];
        return (cell != 1 && cell != 2 && cell != 4);
    }
}