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

    public void loadArena1() {
        reset(false); 
        addOuterBoundaries(); 
    }

    public void loadArena2() {
        reset(false); 
        addOuterBoundaries();
        for(int i=5; i<35; i+=5) {
            grid[i][10] = 3;
            grid[i][30] = 3;
        }
    }

    public void loadArena3() {
        reset(false); 
        addOuterBoundaries();
        for (int i = 10; i < 30; i++) {
            grid[i][10] = 1; 
            grid[i][30] = 1; 
        }
        for (int y = 15; y < 25; y++) {
            grid[20][y] = 1; 
        }
    }

    public void loadRandomArena() {
        reset(true); // Open Type
        Random rand = new Random();
        int obstacleCount = 30 + rand.nextInt(20); 
        for (int i = 0; i < obstacleCount; i++) {
            int rx = rand.nextInt(GRID_SIZE);
            int ry = rand.nextInt(GRID_SIZE);
            if (Math.abs(rx - 20) < 5 && Math.abs(ry - 20) < 5) continue;
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
        int oldX = c.getX();
        int oldY = c.getY();

        // 1. Check Falling Off
        if (nextX < 0 || nextX >= GRID_SIZE || nextY < 0 || nextY >= GRID_SIZE) {
            if (isOpenType) {
                c.reduceLives(100); 
                System.out.println(c.getName() + " fell off the grid!");
            }
            return; 
        }

        // 2. Check Collision
        int targetCell = grid[nextX][nextY];
        
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