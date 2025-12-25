package fop.assignment;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Disc {
    private int x, y;
    private int dx, dy; // Direction of movement
    private boolean active = true; // Is the disc flying or destroyed?

    // Constructor: Spawns the disc at the player's position, flying in their direction
    public Disc(int startX, int startY, int direction) {
        this.x = startX;
        this.y = startY;
        
        // 0=UP, 1=DOWN, 2=LEFT, 3=RIGHT
        // We set the movement speed (1 block per update)
        switch (direction) {
            case 0: dx = 0; dy = -1; break; // UP
            case 1: dx = 0; dy = 1;  break; // DOWN
            case 2: dx = -1; dy = 0; break; // LEFT
            case 3: dx = 1; dy = 0;  break; // RIGHT
            default: active = false; break;
        }
    }

    // Move the disc forward
    public void update() {
        x += dx;
        y += dy;

        // Deactivate if it hits map boundaries (0-39)
        if (x < 0 || x >= 40 || y < 0 || y >= 40) {
            active = false;
        }
    }

    // Draw the disc on the canvas
    public void draw(GraphicsContext gc, int cell) {
        if (!active) return;
        
        // Draw the outer glow (Cyan)
        gc.setFill(Color.CYAN);
        gc.fillOval(x * cell + 2, y * cell + 2, cell - 4, cell - 4);
        
        // Draw the inner white core
        gc.setFill(Color.WHITE);
        gc.fillOval(x * cell + 4, y * cell + 4, cell - 8, cell - 8);
    }

    // --- Getters and Setters ---
    public boolean isActive() { return active; }
    
    public int getX() { return x; }
    public int getY() { return y; }
    
    // Call this when the disc hits an enemy
    public void deactivate() { active = false; }
}
