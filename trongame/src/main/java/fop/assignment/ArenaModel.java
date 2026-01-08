package fop.assignment;

import java.util.Random;

public class ArenaModel {
    public static final int GRID_SIZE = 40;
    
    // 0 = Empty, 1 = Wall, 2 = Player Trail, 3 = Speed Ramp, 4 = Enemy Trail
    private int[][] grid = new int[GRID_SIZE][GRID_SIZE];
    
    private boolean isOpenType = false; 
    private boolean speedBoostActive = false;

    public ArenaModel() {
        // Default constructor
    }

    // LEVEL 1-9: THE GRID (Basic Empty Box)
    public void loadArena1() {
        reset(false); 
        addOuterBoundaries(); 
    }

    // LEVEL 10-19: ACCELERATION (Speed Ramps)
    public void loadArena2() {
        reset(false); 
        addOuterBoundaries();
        
        // Add "Highways" of speed ramps
        for(int i=5; i<35; i++) {
            grid[i][10] = 3; 
            grid[i][30] = 3; 
        }
        for(int j=10; j<30; j++) {
            grid[20][j] = 3; 
        }
    }

    // --- CHANGED: EASIER VERSION ---
    // LEVEL 20-29: THE BUNKER (Sparse Obstacles)
    public void loadArena3() {
        reset(false); 
        addOuterBoundaries();
        
        // Old version was too tight. 
        // New version: 4 Long walls for cover, but wide open center.
        
        // Vertical Walls
        for (int y = 10; y < 30; y++) {
            grid[10][y] = 1;
            grid[30][y] = 1;
        }
        
        // Horizontal Walls (with gap in middle)
        for (int x = 12; x < 29; x++) {
            if (x < 18 || x > 22) { // Leave a gap in the very center
                grid[x][10] = 1;
                grid[x][30] = 1;
            }
        }
    }

    // --- CHANGED: SCALING DIFFICULTY ---
    // LEVEL 30+: THE GLITCH (Randomized & Scaling)
    public void loadRandomArena(int currentLevel) {
        reset(true); // Open Type (Falling is possible)
        
        Random rand = new Random();
        
        // FORMULA: Base 30 obstacles + 2 obstacles for every level above 30
        // Level 30 = 30 obstacles
        // Level 50 = 70 obstacles
        // Level 99 = ~170 obstacles
        int difficultyScaling = (currentLevel - 30) * 2;
        if (difficultyScaling < 0) difficultyScaling = 0;
        
        int obstacleCount = 30 + difficultyScaling;
        
        // Cap the max obstacles so map is not impossible (Max 400 cells are walls)
        if (obstacleCount > 400) obstacleCount = 400;

        for (int i = 0; i < obstacleCount; i++) {
            int rx = rand.nextInt(GRID_SIZE);
            int ry = rand.nextInt(GRID_SIZE);
            
            // Safe Zone: Don't spawn on the center 6x6 area where player starts
            if (Math.abs(rx - 20) < 4 && Math.abs(ry - 20) < 4) continue;
            
            // 80% Wall, 20% Speed Ramp (Glitch)
            if (rand.nextDouble() > 0.8) grid[rx][ry] = 3;
            else grid[rx][ry] = 1;
        }
    }

    // --- CORE LOGIC ---

    private void reset(boolean open) {
        grid = new int[GRID_SIZE][GRID_SIZE];
        this.isOpenType = open;
    }

    private void addOuterBoundaries() {
        for (int i = 0; i < GRID_SIZE; i++) {
            grid[i][0] = 1;
            grid[i][GRID_SIZE - 1] = 1;
            grid[0][i] = 1;
            grid[GRID_SIZE - 1][i] = 1;
        }
    }

    public void processMove(GameCharacter c, int nextX, int nextY) {
        // Check Falling Off (Arena 4 only)
        if (nextX < 0 || nextX >= GRID_SIZE || nextY < 0 || nextY >= GRID_SIZE) {
            // Logic handled in Controller, but model knows it's invalid
            return; 
        }

        // Check Collision
        int targetCell = grid[nextX][nextY];
        
        if (targetCell == 3) this.speedBoostActive = true; 
        else this.speedBoostActive = false;

        // Valid Move - Leave Trail
        if (c instanceof Player) {
            grid[c.getX()][c.getY()] = 2; // Old spot becomes trail
        } else {
            grid[c.getX()][c.getY()] = 4; // Enemy trail
        }
        c.setPosition(nextX, nextY);
    }

    public boolean isSpeedBoostActive() { return speedBoostActive; }
    public int[][] getGrid() { return grid; }
    public boolean isOpenType() { return isOpenType; }
}