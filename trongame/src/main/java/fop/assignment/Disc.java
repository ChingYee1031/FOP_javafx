package fop.assignment;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

public class Disc {
    
    private int x, y;
    private int direction; 
    private boolean active = true;
    private boolean stationary = false;
    private GameCharacter owner;
    
    // --- UPDATED: DISTANCE LIMIT ---
    private int distanceTraveled = 0;
    private final int MAX_DISTANCE = 10; // Changed from 3 to 10

    private static final DropShadow HOVER_EFFECT = new DropShadow();
    static {
        HOVER_EFFECT.setRadius(10.0);
        HOVER_EFFECT.setColor(Color.WHITE);
        HOVER_EFFECT.setSpread(0.4);
    }

    public Disc(int startX, int startY, int direction, GameCharacter owner) {
        this.x = startX;
        this.y = startY;
        this.direction = direction;
        this.owner = owner;
    }

    public void update(int[][] grid) {
        if (!active || stationary) return;

        int nextX = x;
        int nextY = y;

        switch (direction) {
            case 0: nextY--; break; // UP
            case 1: nextY++; break; // DOWN
            case 2: nextX--; break; // LEFT
            case 3: nextX++; break; // RIGHT
        }

        // 1. Check Bounds
        if (nextX < 0 || nextX >= grid.length || nextY < 0 || nextY >= grid[0].length) {
            becomeStationary();
            return;
        }

        // 2. Check Walls/Trails
        int cell = grid[nextX][nextY];
        if (cell == 1 || cell == 2 || cell == 4) {
            becomeStationary();
        } else {
            this.x = nextX;
            this.y = nextY;
            
            // Increment Distance
            distanceTraveled++;
            if (distanceTraveled >= MAX_DISTANCE) {
                becomeStationary(); // Drop to ground after 10 steps
            }
        }
    }

    public void returnToOwner() {
        this.active = false;
        if (owner != null) {
            owner.recoverAmmo();
        }
    }

    private void becomeStationary() {
        this.stationary = true;
    }

    public void draw(GraphicsContext gc, int cellWidth) {
        if (!active) return;

        double drawX = x * cellWidth;
        double drawY = y * cellWidth;
        
        gc.save(); 
        gc.setEffect(HOVER_EFFECT);
        
        // Outer Ring (Owner Color)
        gc.setFill(Color.web(owner.getColor()));
        gc.fillOval(drawX + 2, drawY + 2, cellWidth - 4, cellWidth - 4);
        
        // Inner Core (White for visibility)
        gc.setFill(Color.WHITE);
        gc.fillOval(drawX + 5, drawY + 5, cellWidth - 10, cellWidth - 10);
        
        gc.restore(); 
    }

    public boolean isActive() { return active; }
    public boolean isStationary() { return stationary; }
    public int getX() { return x; }
    public int getY() { return y; }
    public GameCharacter getOwner() { return owner; }
}