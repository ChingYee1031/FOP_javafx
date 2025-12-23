package fop.assignment;
import java.util.Random;

public class ArenaModel {
    // Requirements: 40x40 grid-based arena
    public static final int GRID_SIZE = 40;
    
    // Cell types: 0 = Empty, 1 = Boundary/Obstacle, 2 = Jetwall, 3 = Speed Ramp
    private int[][] grid = new int[GRID_SIZE][GRID_SIZE];
    
    private boolean isOpenType = false; // "Open-type grid" rule
    private boolean speedBoostActive = false;

    public ArenaModel() {
        loadArena1(); // Default start
    }

    // --- ARENA DESIGNS ---

    public void loadArena1() {
        reset(false);
        addOuterBoundaries();
    }

    public void loadArena2() {
        reset(false);
        addOuterBoundaries();
        // Speed ramp placement
        grid[10][10] = 3; 
        grid[30][10] = 3;
        grid[10][30] = 3;
        grid[30][30] = 3;
    }

    public void loadArena3() {
        reset(false);
        addOuterBoundaries();
        // Internal obstacles
        for (int i = 15; i < 25; i++) {
            grid[i][15] = 1;
            grid[i][25] = 1;
        }
    }

    public void loadRandomArena() {
        reset(true); // Open-type grid (no boundaries)
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            int x = rand.nextInt(GRID_SIZE);
            int y = rand.nextInt(GRID_SIZE);
            grid[x][y] = 1;
        }
    }

    // --- GAME LOGIC ---

    private void reset(boolean open) {
        grid = new int[GRID_SIZE][GRID_SIZE];
        // REMOVED: playerLives = 3.0; (This is now inside the Player object)
        isOpenType = open;
    }

    private void addOuterBoundaries() {
        for (int i = 0; i < GRID_SIZE; i++) {
            grid[i][0] = 1;             // Top
            grid[i][GRID_SIZE - 1] = 1; // Bottom
            grid[0][i] = 1;             // Left
            grid[GRID_SIZE - 1][i] = 1; // Right
        }
    }

    // Logic to handle movement for BOTH Player and Enemy [cite: 15]
    public void processMove(GameCharacter c, int nextX, int nextY) {
        int oldX = c.getX();
        int oldY = c.getY();

        // 1. Check Falling Off (Open Grid Rule) [cite: 71]
        if (nextX < 0 || nextX >= GRID_SIZE || nextY < 0 || nextY >= GRID_SIZE) {
            if (isOpenType) {
                c.reduceLives(100);// Instant death [cite: 71]
            }
            return; 
        }

         //2. Check Collisions with Walls (1) or Jetwalls (2) [cite: 70]
        int targetCell = grid[nextX][nextY];
        
        if (targetCell == 1 || targetCell == 2) {
            c.reduceLives(0.5); // Lose 0.5 lives [cite: 70]
            // Do NOT update position (bounce effect)
        } 
        else {
            // 3. Valid Move
            // Leave a Jetwall (2) behind at the OLD position [cite: 69]
            grid[oldX][oldY] = 2; 

            // Update Character Position inside the object
            c.setPosition(nextX, nextY);
        }
    }

    // --- GETTERS ---
    public boolean isSpeedBoostActive() { 
        return speedBoostActive; 
    }
    
    public int[][] getGrid() {
        return grid;
    }

    public boolean isOpenType() {
        return isOpenType; 
    }

    // REMOVED: public double getPlayerLives() 
    // (We removed this because the Controller now checks 'player.getLives()' directly)
}
