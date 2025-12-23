package fop.assignment;

import java.util.ArrayList;
import java.util.Random;

public class Enemy extends GameCharacter {
    // New fields required by the assignment
    private String difficulty; 
    private int xpReward;
    private String intelligence; 

    public Enemy(String name, String color, double lives, double speed, String difficulty, int xpReward, String intelligence) {
        super(name, color, lives, speed);
        this.difficulty = difficulty;
        this.xpReward = xpReward;
        this.intelligence = intelligence;
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

        if (validMoves.isEmpty()) return -1; // No moves, crash imminent

        Random rand = new Random();
        int chosenMove = -1;

        // --- AI PERSONALITY LOGIC ---
        if (intelligence.equals("Predictable")) {
            // Sark: 80% chance to keep moving forward (Current Direction) [cite: 144]
            // We check 'currentDir != -1' to ensure we have moved at least once before
            if (currentDir != -1 && validMoves.contains(currentDir)) {
                if (rand.nextDouble() > 0.2) {
                    chosenMove = currentDir;
                }
            }
        }
        else if (intelligence.equals("Erratic")) {
            // Koura: Purely random [cite: 148]
            chosenMove = validMoves.get(rand.nextInt(validMoves.size()));
        }

        // Default Fallback (if no specific move was chosen above)
        if (chosenMove == -1) {
            chosenMove = validMoves.get(rand.nextInt(validMoves.size()));
        }

        // CRITICAL FIX: Save the direction so we remember it for next time!
        this.currentDir = chosenMove; 
        
        return chosenMove;
    }

    private boolean isSafe(int tx, int ty, int[][] grid) {
        if (tx < 0 || tx >= 40 || ty < 0 || ty >= 40) return false;
        return (grid[tx][ty] != 1 && grid[tx][ty] != 2);
    }
}
