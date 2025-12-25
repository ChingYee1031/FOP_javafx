package fop.assignment;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class ArenaRenderer {
    private final int CELL = 15;

    public void render(GraphicsContext gc, ArenaModel model, Character selectedChar, int playerX, int playerY) {
        // 1. Clear Screen
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 600, 600);

        // 2. Draw HUD with Glow
        drawHUD(gc, model, selectedChar);

        // 3. Draw Grid
        int[][] grid = model.getGrid();
        for (int x = 0; x < 40; x++) {
            for (int y = 0; y < 40; y++) {
                if (grid[x][y] == 1) gc.setFill(Color.WHITE);
                else if (grid[x][y] == 2) gc.setFill(Color.web("#00FFFF", 0.5));
                else if (grid[x][y] == 3) gc.setFill(Color.YELLOW);
                else continue;
                gc.fillRect(x * CELL, y * CELL, CELL - 1, CELL - 1);
            }
        }

        // 4. Draw Player Head
        if (model.getPlayerLives() <= 0) {
            gc.setFill(Color.RED);
        } else {
            String hexColor = selectedChar.getColor();
            gc.setFill((hexColor == null || hexColor.isEmpty()) ? Color.LIME : Color.web(hexColor));
        }
        gc.fillRect(playerX * CELL, playerY * CELL, CELL - 1, CELL - 1);
    }

   private void drawHUD(GraphicsContext gc, ArenaModel model, Character selectedChar) {
    // 1. Calculate lives (rounding up ensures half-lives show a heart)
    int currentLives = (int) Math.ceil(model.getPlayerLives());
    
    // 2. Set the HUD color based on the selected character's theme
    String charColor = selectedChar.getColor();
    Color hudColor = (charColor == null || charColor.isEmpty()) ? Color.LIME : Color.web(charColor);

    // 3. Draw the Text (Clean, no glow)
    gc.setFill(hudColor); 
    gc.setFont(new Font("OCR A Extended", 20)); 
    gc.fillText("USER LIVES:", 20, 35); 

    // 5. Draw the Heart Icons
    gc.setFill(Color.RED);
    for (int i = 0; i < currentLives; i++) {
    double x = 160 + (i * 30); // Slightly more spacing
    double y = 20;
    double size = 12;

    // Left Lobe
    gc.fillOval(x, y, size, size);
    // Right Lobe
    gc.fillOval(x + size * 0.7, y, size, size);
    
    // Bottom Triangle (Using polygon for a sharp point)
    double[] xPoints = { x, x + size * 1.7, x + size * 0.85 };
    double[] yPoints = { y + size * 0.6, y + size * 0.6, y + size * 1.6 };
    gc.fillPolygon(xPoints, yPoints, 3);
    }
    }
}