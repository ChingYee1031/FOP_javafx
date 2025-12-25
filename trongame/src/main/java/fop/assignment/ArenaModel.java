package fop.assignment;
import java.util.Random;

public class ArenaModel {
    // Requirements: 40x40 grid-based arena
    public static final int GRID_SIZE = 40;
    
    // Cell types: 0 = Empty, 1 = Boundary/Obstacle, 2 = Jetwall, 3 = Speed Ramp
    private int[][] grid = new int[GRID_SIZE][GRID_SIZE];
    
    private double playerLives = 3.0;
    private boolean isOpenType = false; // "Open-type grid" rule
    private boolean speedBoostActive = false;

    public ArenaModel() {
        loadArena1(); // Default start
    }

    // --- ARENA DESIGNS ---

    /**
     * Predesigned Arena 1: Classic Box
     * Standard boundary walls.
     */
    public void loadArena1() {
        reset(false);
        addOuterBoundaries();
    }

    /**
     * Predesigned Arena 2: The Circuit
     * Features outer walls and specific Speed Ramp placement.
     */
    public void loadArena2() {
        reset(false);
        addOuterBoundaries();
        // Speed ramp placement
        grid[10][10] = 3; 
        grid[30][10] = 3;
        grid[10][30] = 3;
        grid[30][30] = 3;
    }

    /**
     * Predesigned Arena 3: Obstacle Arrangement
     * Differing layout with internal pillars.
     */
    public void loadArena3() {
        reset(false);
        addOuterBoundaries();
        // Internal obstacles
        for (int i = 15; i < 25; i++) {
            grid[i][15] = 1;
            grid[i][25] = 1;
        }
    }

    /**
     * Randomly Generated Arena Option
     * Set as "open-type" (no boundaries). Falling off results in loss of all lives.
     */
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
        playerLives = 3.0;
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

   public void processMove(int x, int y, int oldX, int oldY) {
    speedBoostActive = false; // Reset speed boost each cycle

    // 1. Check for "Falling Off" in open-type grid
    // If coordinates are outside the 0-39 range
    if (x < 0 || x >= GRID_SIZE || y < 0 || y >= GRID_SIZE) {
        if (isOpenType) {
            // Requirement: Falling off results in loss of all remaining lives
            playerLives = 0; 
        }
        return; // Stop processing further for this move
    }

    // 2. Check for collisions with walls or jetwalls
    int targetCell = grid[x][y];
    if (targetCell == 1 || targetCell == 2) {
        // Hitting wall or jetwall causes -0.5 lives
        playerLives -= 0.5;
    } else if (targetCell == 3) {
        // Handle Speed Ramp
        speedBoostActive = true; 
    }

    // 3. Mark the OLD position as an impassable jetwall
    grid[oldX][oldY] = 2;
    }

    // --- GETTERS ---
    public boolean isSpeedBoostActive() { 
        return speedBoostActive; }
    public int[][] getGrid() {
        return grid;}
    public double getPlayerLives() {
        return playerLives;}
    public boolean isOpenType() {
        return isOpenType; }
    
    public void resetLivesAndGrid(int arenaNumber) {
    this.playerLives = 3; // Or your starting lives constant
    // Re-load the specific arena to clear old jetwalls (grid = 0)
    if (arenaNumber == 1) loadArena1();
    else if (arenaNumber == 2) loadArena2();
    else if (arenaNumber == 3) loadArena3();
}
}
