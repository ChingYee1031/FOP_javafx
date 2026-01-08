package fop.assignment;

import java.util.Random;

public class ArenaModel {
    public static final int GRID_SIZE = 40;
    
    // 0 = Empty, 1 = Wall/Obstacle, 2 = Player Trail, 3 = Speed Ramp, 4 = Enemy Trail
    private int[][] grid = new int[GRID_SIZE][GRID_SIZE];
    
    private boolean isOpenType = false; 
    private boolean speedBoostActive = false;

    public ArenaModel() {
        // Default constructor
    }

    // --- ARENA GENERATION LOGIC ---

    // LEVEL 1-9: THE GRID (Basic Empty Box)
    public void loadArena1() {
        reset(false); 
        addOuterBoundaries(); 
        // A clean slate for beginners
    }

    // LEVEL 10-19: ACCELERATION (Speed Ramps)
    public void loadArena2() {
        reset(false); 
        addOuterBoundaries();
        
        // Add "Highways" of speed ramps (Yellow/Cyan in UI)
        for(int i=5; i<35; i++) {
            grid[i][10] = 3; // Top horizontal ramp
            grid[i][30] = 3; // Bottom horizontal ramp
        }
        for(int j=10; j<30; j++) {
            grid[20][j] = 3; // Central vertical ramp
        }
    }

    // LEVEL 20-29: THE BUNKER (Close Quarters Obstacles)
    public void loadArena3() {
        reset(false); 
        addOuterBoundaries();
        
        // Create a "Pillar" layout to force tight maneuvering
        for (int x = 5; x < 35; x += 5) {
            for (int y = 5; y < 35; y += 5) {
                // Create 2x2 blocks of walls
                grid[x][y] = 1;
                grid[x+1][y] = 1;
                grid[x][y+1] = 1;
                grid[x+1][y+1] = 1;
            }
        }
        
        // Clear the center for spawning
        for(int x=18; x<=22; x++){
            for(int y=18; y<=22; y++){
                grid[x][y] = 0;
            }
        }
    }

    // LEVEL 30+: THE GLITCH (Randomized Open Grid)
    // "Falling off the grid will result in immediate derezzing"
    public void loadRandomArena() {
        reset(true); // <--- OPEN TYPE (No outer walls, can fall off)
        
        Random rand = new Random();
        int obstacleCount = 40 + rand.nextInt(20); 
        
        for (int i = 0; i < obstacleCount; i++) {
            int rx = rand.nextInt(GRID_SIZE);
            int ry = rand.nextInt(GRID_SIZE);
            
            // Don't spawn obstacles on the player start zone (Center)
            if (Math.abs(rx - 20) < 5 && Math.abs(ry - 20) < 5) continue;
            
            // 70% Chance of Speed Ramp (Glitchy terrain), 30% Wall
            if (rand.nextDouble() > 0.3) grid[rx][ry] = 3;
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
        int oldX = c.getX();
        int oldY = c.getY();

        // 1. Check Falling Off (Only happens in Arena 4 / Open Type)
        if (nextX < 0 || nextX >= GRID_SIZE || nextY < 0 || nextY >= GRID_SIZE) {
            if (isOpenType) {
                c.reduceLives(100); // Instant Kill
                System.out.println(c.getName() + " fell off the grid!");
            }
            return; 
        }

        // 2. Check Collision
        int targetCell = grid[nextX][nextY];
        
        // Check for Speed Ramp (3)
        if (targetCell == 3) this.speedBoostActive = true; 
        else this.speedBoostActive = false;

        // Hit Wall (1), Player Trail (2), or Enemy Trail (4)
        if (targetCell == 1 || targetCell == 2 || targetCell == 4) {
            c.reduceLives(0.5); 
        } else {
            // Valid Move - Leave Trail
            if (c instanceof Player) {
                grid[oldX][oldY] = 2; // Player Trail
            } else {
                grid[oldX][oldY] = 4; // Enemy Trail
            }
            c.setPosition(nextX, nextY);
        }
    }

    public boolean isSpeedBoostActive() { return speedBoostActive; }
    public int[][] getGrid() { return grid; }
    public boolean isOpenType() { return isOpenType; }
}