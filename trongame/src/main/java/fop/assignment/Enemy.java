package fop.assignment;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import javafx.scene.image.Image;

public class Enemy extends GameCharacter {

    private String difficulty; 
    private int xpReward;
    private String intelligence; 
    
    // NEW: Variable to store the image
    private Image enemyIcon;

    public Enemy(String name, String color, double lives, double speed, String difficulty, int xpReward, String intelligence) {
        super(name, color, lives, speed);
        this.difficulty = difficulty;
        this.xpReward = xpReward;
        this.intelligence = intelligence;
        
        // NEW: Load the image as soon as the enemy is created
        loadEnemyImage();
    }

    private void loadEnemyImage() {
        try {
            // This looks for "images/Clu.png", "images/Sark.png" etc.
            // based on the name from the text file.
            String path = "images/" + this.name + ".png";
            File file = new File(path);
            
            if (file.exists()) {
                this.enemyIcon = new Image(file.toURI().toString());
            } else {
                System.out.println("Warning: Could not find image at " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // NEW: Allow the controller to get the image
    public Image getIcon() {
        return this.enemyIcon;
    }

    public int getXPReward() {
        return this.xpReward;
    }

    // AI Logic
    public int makeMove(int[][] grid) {
        if (!isAlive()) return -1;

        ArrayList<Integer> validMoves = new ArrayList<>();
        // Check 0=UP, 1=DOWN, 2=LEFT, 3=RIGHT
        if (isSafe(x, y - 1, grid)) validMoves.add(0); 
        if (isSafe(x, y + 1, grid)) validMoves.add(1); 
        if (isSafe(x - 1, y, grid)) validMoves.add(2); 
        if (isSafe(x + 1, y, grid)) validMoves.add(3);

        if (validMoves.isEmpty()) return -1; 

        Random rand = new Random();
        int chosenMove = -1;

        // --- AI PERSONALITY LOGIC ---
        if (intelligence.equals("Predictable")) {
            // Sark: Stick to current direction (80% chance)
            if (currentDir != -1 && validMoves.contains(currentDir)) {
                if (rand.nextDouble() > 0.2) {
                    chosenMove = currentDir;
                }
            }
        }
        else if (intelligence.equals("Erratic")) {
            // Koura: Purely random
            chosenMove = validMoves.get(rand.nextInt(validMoves.size()));
        }

        if (chosenMove == -1) {
            chosenMove = validMoves.get(rand.nextInt(validMoves.size()));
        }

        this.currentDir = chosenMove; 
        return chosenMove;
    }

    private boolean isSafe(int tx, int ty, int[][] grid) {
        if (tx < 0 || tx >= 40 || ty < 0 || ty >= 40) return false;
        return (grid[tx][ty] != 1 && grid[tx][ty] != 2);
    }
}