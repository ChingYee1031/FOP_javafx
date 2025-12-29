package fop.assignment;

import java.util.Random;

public class ArenaModel {
    public static final int GRID_SIZE = 40;
    
    // 0 = Empty, 1 = Wall/Obstacle, 2 = Jetwall, 3 = Speed Ramp
    private int[][] grid = new int[GRID_SIZE][GRID_SIZE];
    
    private boolean isOpenType = false; 
    private boolean speedBoostActive = false;

    public ArenaModel() {
        // Default constructor, actual loading happens in Controller
    }

    // --- ARENA GENERATION LOGIC ---

    // Arena 1: Basic Training (Levels 1-9)
    public void loadArena1() {
        reset(false); // Closed Boundary
        addOuterBoundaries(); 
        // No obstacles, pure combat
    }

    // Arena 2: The Accelerator (Levels 10-19)
    public void loadArena2() {
        reset(false); // Closed Boundary
        addOuterBoundaries();
        
        // Add Speed Ramps (Value 3) in a pattern
        // Requirement: Speed ramp placement 
        for(int i=5; i<35; i+=5) {
            grid[i][10] = 3;
            grid[i][30] = 3;
        }
    }

    // Arena 3: The Bunker (Levels 20-29)
    public void loadArena3() {
        reset(false); // Closed Boundary
        addOuterBoundaries();
        
        // Add Fixed Obstacles (Value 1) to create cover
        // Requirement: Obstacle arrangement 
        for (int i = 10; i < 30; i++) {
            grid[i][10] = 1; // Top horizontal wall
            grid[i][30] = 1; // Bottom horizontal wall
        }
        for (int y = 15; y < 25; y++) {
            grid[20][y] = 1; // Center vertical pillar
        }
    }

    // Arena 4: Procedural "Glitch" (Level 30+)
    // Requirement: Random arena generator 
    public void loadRandomArena() {
        reset(true); // OPEN BOUNDARY (Falling off = Death) [cite: 71]
        
        // We DO NOT add outer boundaries here.
        
        Random rand = new Random();
        
        // Generate random obstacles
        int obstacleCount = 30 + rand.nextInt(20); // 30 to 50 obstacles
        
        for (int i = 0; i < obstacleCount; i++) {
            int rx = rand.nextInt(GRID_SIZE);
            int ry = rand.nextInt(GRID_SIZE);
            
            // Safety Check: Don't spawn obstacles in the center (Player Spawn Zone)
            if (Math.abs(rx - 20) < 5 && Math.abs(ry - 20) < 5) continue;
            
            // Randomly decide: 80% Static Wall, 20% Speed Ramp
            if (rand.nextDouble() > 0.8) {
                grid[rx][ry] = 3;
            } else {
                grid[rx][ry] = 1;
            }
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
        // Logic remains the same as your original file
        int oldX = c.getX();
        int oldY = c.getY();

        // 1. Check Falling Off (Open Grid Rule) [cite: 71]
        if (nextX < 0 || nextX >= GRID_SIZE || nextY < 0 || nextY >= GRID_SIZE) {
            if (isOpenType) {
                c.reduceLives(100); // Instant Death
                System.out.println(c.getName() + " fell off the grid!");
            }
            return; 
        }

        // 2. Check Collision
        int targetCell = grid[nextX][nextY];
        
        if (targetCell == 3) {
            // Speed Ramp Logic: You might want to implement a temporary speed boost here
             this.speedBoostActive = true; 
        } else {
             this.speedBoostActive = false;
        }

        if (targetCell == 1 || targetCell == 2) {
            c.reduceLives(0.5); // Wall Collision
        } else {
            // Valid Move
            grid[oldX][oldY] = 2; // Leave jetwall
            c.setPosition(nextX, nextY);
        }
    }

    public boolean isSpeedBoostActive() { return speedBoostActive; }
    public int[][] getGrid() { return grid; }
    public boolean isOpenType() { return isOpenType; }
}