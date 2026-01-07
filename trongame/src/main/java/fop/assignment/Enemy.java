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

    // Shooting
    private long lastShotTime = 0;
    private static final long SHOOT_DELAY = 2_000_000_000L; 

    public Enemy(String name, String color, double lives, double speed, String difficulty, int xpReward, String intelligence) {
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

    // --- AI MOVEMENT ---
    public int makeMove(int[][] grid) {
        if (!isAlive()) return -1;

        ArrayList<Integer> validMoves = new ArrayList<>();
        // 0=UP, 1=DOWN, 2=LEFT, 3=RIGHT
        if (isSafe(x, y - 1, grid)) validMoves.add(0); 
        if (isSafe(x, y + 1, grid)) validMoves.add(1); 
        if (isSafe(x - 1, y, grid)) validMoves.add(2); 
        if (isSafe(x + 1, y, grid)) validMoves.add(3);

        if (validMoves.isEmpty()) return -1; 

        Random rand = new Random();
        int chosenMove = -1;

        if (intelligence.equals("Predictable")) {
            if (currentDir != -1 && validMoves.contains(currentDir)) {
                if (rand.nextDouble() > 0.2) {
                    chosenMove = currentDir;
                }
            }
        }
        else if (intelligence.equals("Erratic")) {
            chosenMove = validMoves.get(rand.nextInt(validMoves.size()));
        }

        if (chosenMove == -1) {
            chosenMove = validMoves.get(rand.nextInt(validMoves.size()));
        }

        this.currentDir = chosenMove; 
        return chosenMove;
    }

    // --- CRITICAL FIX HERE ---
    private boolean isSafe(int tx, int ty, int[][] grid) {
        // 1. Bounds Check
        if (tx < 0 || tx >= 40 || ty < 0 || ty >= 40) return false;
        
        // 2. Obstacle Check
        // 1 = Wall, 2 = Player Trail, 4 = Enemy Trail
        // The enemy must avoid ALL of these.
        int cell = grid[tx][ty];
        return (cell != 1 && cell != 2 && cell != 4);
    }
}