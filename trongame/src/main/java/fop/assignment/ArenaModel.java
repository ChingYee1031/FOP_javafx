package fop.assignment;

import java.util.ArrayList;
import java.util.Random;
import java.util.Iterator;

public class ArenaModel {
    public static final int GRID_SIZE = 40;
    
    // 0 = Empty, 1 = Wall, 2 = Player Trail, 3 = Speed Ramp, 4 = Enemy Trail
    private int[][] grid = new int[GRID_SIZE][GRID_SIZE];
    
    private boolean isOpenType = false; 
    private boolean speedBoostActive = false;

    // for speed ramp respawn
    private final long RAMP_RESPAWN_TIME = 45_000_000_000L;

    public ArenaModel() {
        
    }

    // LEVEL 1-9: THE GRID 
    public void loadArena1() {
        reset(false); 
        addOuterBoundaries(); 
    }

    // LEVEL 10-19: ACCELERATION
    public void loadArena2() {
        reset(false); 
        addOuterBoundaries();
        
        // Add speed ramps
        for(int i=5; i<35; i++) {
            grid[i][10] = 3; 
            grid[i][30] = 3; 
        }
        for(int j=10; j<30; j++) {
            grid[20][j] = 3; 
        }
    }
    // LEVEL 20-29: THE BUNKER 
    public void loadArena3() {
        reset(false); 
        addOuterBoundaries();
        // Vertical Walls
        for (int y = 10; y < 30; y++) {
            grid[10][y] = 1;
            grid[30][y] = 1;
        }
        
        // Horizontal Walls 
        for (int x = 12; x < 29; x++) {
            if (x < 18 || x > 22) { // Leave a gap in the very center
                grid[x][10] = 1;
                grid[x][30] = 1;
            }
        }
    }

    // LEVEL 30+: THE GLITCH 
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

    // CORE LOGIC

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

    private class RampTask {
        int x, y;
        long timeUsed;
        
        RampTask(int x, int y, long timeUsed) {
            this.x = x; 
            this.y = y; 
            this.timeUsed = timeUsed;
        }
    }

    private ArrayList <RampTask> usedRamps = new ArrayList<>();

    public void processMove(GameCharacter c, int nextX, int nextY) {
        // Check Falling Off (Arena 4 only)
        if (nextX < 0 || nextX >= GRID_SIZE || nextY < 0 || nextY >= GRID_SIZE) {
            return; 
        }

        // Check Collision
        int targetCell = grid[nextX][nextY];
        
        if (targetCell == 3) {
            this.speedBoostActive = true;
            usedRamps.add(new RampTask(nextX, nextY, System.nanoTime()));
        } 
        else this.speedBoostActive = false;

        // Valid Move - Leave Trail
        if (c instanceof Player) {
            grid[c.getX()][c.getY()] = 2; // Old spot becomes trail
        } else {
            grid[c.getX()][c.getY()] = 4; // Enemy trail
        }
        c.setPosition(nextX, nextY);
    }

    public void updateRampRespawns() {
        if (usedRamps.isEmpty()) return;

        long now = System.nanoTime();
        Iterator<RampTask> it = usedRamps.iterator();

        while (it.hasNext()) {
            RampTask task = it.next();
            // If 45 seconds have passed...
            if (now - task.timeUsed >= RAMP_RESPAWN_TIME) {
                
                // RESTORE THE RAMP
                if (grid[task.x][task.y] != 1) {
                    grid[task.x][task.y] = 3;
                }
                
                // Remove from the wait list
                it.remove();
            }
        }
    }

    public boolean isSpeedBoostActive() { return speedBoostActive; }
    public int[][] getGrid() { return grid; }
    public boolean isOpenType() { return isOpenType; }

    
}