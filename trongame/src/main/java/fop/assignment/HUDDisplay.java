package fop.assignment;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class HUDDisplay {
    private static final Font TRON_FONT = new Font("OCR A Extended", 20);
    public static void draw(GraphicsContext gc, int lives, String hexColor) {
        // Draw Text
        gc.setFill(Color.WHITE);
        gc.setFont(TRON_FONT);
        gc.fillText("USER LIVES:", 20, 35);
        // Draw Hearts
        gc.setFill(Color.RED);
        for (int i = 0; i < lives; i++) {
            double x = 160 + (i * 25);
            double y = 20;
            gc.fillOval(x, y, 10, 10);
            gc.fillOval(x + 7, y, 10, 10);
            gc.fillRect(x + 2, y + 5, 13, 10);
        }
    }
    public static void drawGameOver(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(new javafx.scene.text.Font("Arial", 18));
        String message = "DEREZZED - SELECT AN ARENA TO TRY AGAIN";
        gc.fillText(message, 110, 150);
    }
}