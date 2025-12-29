package fop.assignment;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Disc {
    private int x, y;
    private int dir; // 0=Up, 1=Down, 2=Left, 3=Right
    private boolean active = true;
    
    // --- NEW LOGIC FIELDS ---
    private GameCharacter owner;
    private int startX, startY;
    private boolean isStationary = false; // False = Flying, True = On Ground
    private long landTime = 0;
    private static final long COOLDOWN_NANOS = 3_000_000_000L; // 5 Seconds
    
    public Disc(int x, int y, int dir, GameCharacter owner) {
        this.x = x;
        this.y = y;
        this.startX = x;
        this.startY = y;
        this.dir = dir;
        this.owner = owner;
    }

    public void update(int[][] grid) {
        if (!active) return;

        // 1. STATIONARY LOGIC (Waiting on ground)
        if (isStationary) {
            // Check 5-second cooldown
            if (System.nanoTime() - landTime > COOLDOWN_NANOS) {
                returnToOwner(); // Time's up! Return automatically.
            }
            return; // Don't move
        }

        // 2. FLYING LOGIC
        int dist = Math.abs(x - startX) + Math.abs(y - startY);
        if (dist >= 10) {
            land(); // Reached max range
            return;
        }

        // Calculate next step
        int nextX = x;
        int nextY = y;
        
        switch (dir) {
            case 0: nextY--; break; // Up
            case 1: nextY++; break; // Down
            case 2: nextX--; break; // Left
            case 3: nextX++; break; // Right
        }

        // Wall Collision Check
        if (nextX < 0 || nextX >= 40 || nextY < 0 || nextY >= 40 || grid[nextX][nextY] == 1) {
            land(); // Hit a wall, stop here
        } else {
            // Move
            x = nextX;
            y = nextY;
        }
    }

    private void land() {
        this.isStationary = true;
        this.landTime = System.nanoTime();
    }

    public void returnToOwner() {
        this.active = false; // Remove from screen
        if (owner != null) {
            owner.recoverAmmo(); // Give ammo back to owner
        }
    }

    public void draw(GraphicsContext gc, int cell) {
        if (!active) return;

        if (isStationary) {
            // STATIONARY: Dimmed color + Ring
            gc.setFill(Color.GRAY);
            gc.fillOval(x * cell + 4, y * cell + 4, cell - 8, cell - 8);
            
            gc.setStroke(Color.web(owner.getColor()));
            gc.setLineWidth(2);
            gc.strokeOval(x * cell + 4, y * cell + 4, cell - 8, cell - 8);
        } else {
            // FLYING: Bright Owner Color
            gc.setFill(Color.web(owner.getColor())); 
            gc.fillOval(x * cell + 2, y * cell + 2, cell - 4, cell - 4);
        }
    }

    // Getters
    public boolean isActive() { return active; }
    public boolean isStationary() { return isStationary; }
    public GameCharacter getOwner() { return owner; }
    public int getX() { return x; }
    public int getY() { return y; }
    public void deactivate() { this.active = false; }
}