package fop.assignment;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class ArenaController {
    @FXML private VBox characterMenu; 
    @FXML private VBox arenaMenu;     
    @FXML private Canvas gameCanvas;

    private Character selectedChar;
    private ArenaModel model = new ArenaModel();
    private int playerX = 20;
    private int playerY = 20;
    private final int CELL = 15;

    private enum Direction { UP, DOWN, LEFT, RIGHT, NONE }
    private Direction currentDir = Direction.NONE;
    
    private long lastUpdate = 0;
    private long speedNanos = 100_000_000; 
    private boolean gameStarted = false;

    public void initialize() {
        // 1. Grab the character selected in the menu from the global 'App' slot
        if (App.chosenCharacter != null) {
            this.selectedChar = App.chosenCharacter;
            System.out.println("Arena started with: " + selectedChar.getName());
        } else {
            // Safety fallback: load Tron if no selection was made
            this.selectedChar = new Tron();
            this.selectedChar.loadAttributes("Tron");
        }

        // 2. Initial UI State: Hide character selection, show arena selection
        if (characterMenu != null) characterMenu.setVisible(false);
        if (arenaMenu != null) arenaMenu.setVisible(true);

        model.loadArena1(); 
        draw();
        startTaskTimer();
    }

    // --- Arena Button Actions ---
    @FXML
    private void loadArena1Action() {
        model.loadArena1();
        startGameSession();
    }

    @FXML
    private void loadArena2Action() {
        model.loadArena2();
        startGameSession();
    }

    @FXML
    private void loadArena3Action() {
        model.loadArena3();
        startGameSession();
    }

    @FXML
    private void loadRandomArenaAction() {
        model.loadRandomArena();
        startGameSession();
    } 

    private void startGameSession() {
        resetPlayer();
        gameStarted = true;
        
        // Hide all menu overlays to play
        if (characterMenu != null) characterMenu.setVisible(false);
        if (arenaMenu != null) arenaMenu.setVisible(false);
        
        gameCanvas.requestFocus(); 
        draw();
    }

    private void startTaskTimer() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameStarted) {
                    // Use the speed attribute from the selected character
                    // We adjust speedNanos based on the character's speed value
                    long currentDelay = (long) (speedNanos / selectedChar.getSpeed());
                    if (model.isSpeedBoostActive()) currentDelay /= 2;

                    if (now - lastUpdate >= currentDelay) {
                        updateGame();
                        draw();
                        lastUpdate = now;
                    }
                }
            }
        }.start();
    }   

    private void updateGame() {
        if (currentDir == Direction.NONE || model.getPlayerLives() <= 0) return;

        int nextX = playerX;
        int nextY = playerY;

        switch (currentDir) {
            case UP:    nextY--; break;
            case DOWN:  nextY++; break;
            case LEFT:  nextX--; break;
            case RIGHT: nextX++; break;
            default:    break;
        }

        model.processMove(nextX, nextY, playerX, playerY);

        if (nextX >= 0 && nextX < 40 && nextY >= 0 && nextY < 40) {
            playerX = nextX;
            playerY = nextY;
        }
    }

    @FXML
    public void handleKeyPress(KeyEvent event) {
        if (!gameStarted) return;
        switch (event.getCode()) {
            case W: if (currentDir != Direction.DOWN) currentDir = Direction.UP; break;
            case S: if (currentDir != Direction.UP)   currentDir = Direction.DOWN; break;
            case A: if (currentDir != Direction.RIGHT) currentDir = Direction.LEFT; break;
            case D: if (currentDir != Direction.LEFT)  currentDir = Direction.RIGHT; break;
            default: break;
        }
    }

    private void resetPlayer() {
        playerX = 20;
        playerY = 20;
        currentDir = Direction.NONE;
    }

   private void draw() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        
        // 1. Clear Screen
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 600, 600);

        // 2. Draw Game Grid
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

        // 3. Draw HUD (Hearts and Lives)
        drawHUD(gc);

        // 4. Draw Player Head
        if (model.getPlayerLives() <= 0) {
            gc.setFill(Color.RED);
        } else {
            String hexColor = selectedChar.getColor();
            gc.setFill((hexColor == null || hexColor.isEmpty()) ? Color.LIME : Color.web(hexColor));
        }
        gc.fillRect(playerX * CELL, playerY * CELL, CELL - 1, CELL - 1);

        // --- NEW: Handle Death / Restart Selection Menu ---
        if (model.getPlayerLives() <= 0) {
            gameStarted = false; // Stop game movement
            
            // Bring back the Arena selection buttons
            if (arenaMenu != null) {
                arenaMenu.setVisible(true);
            }

            // Draw centered DEREZZED message
            gc.setFill(Color.WHITE);
            gc.setFont(new javafx.scene.text.Font("Arial", 20)); 
            gc.fillText("DEREZZED - SELECT AN ARENA TO TRY AGAIN", 100, 150);
        }
    } // <--- This bracket correctly ends the draw() method

    private void drawHUD(GraphicsContext gc) {
        int currentLives = (int) model.getPlayerLives();
        
        // Use character color for HUD text
        String charColor = selectedChar.getColor();
        Color hudColor = (charColor == null || charColor.isEmpty()) ? Color.LIME : Color.web(charColor);
        gc.setFill(hudColor); 

        gc.setFont(new javafx.scene.text.Font("OCR A Extended", 20));
        gc.fillText("USER LIVES:", 20, 35);

        // Draw Heart Icons next to the text
        gc.setFill(Color.RED);
        for (int i = 0; i < currentLives; i++) {
            double xOffset = 160 + (i * 25);
            double yOffset = 20;
            gc.fillOval(xOffset, yOffset, 10, 10);         // Left bump
            gc.fillOval(xOffset + 7, yOffset, 10, 10);     // Right bump
            gc.fillRect(xOffset + 2, yOffset + 5, 13, 10); // Bottom fill
        }
    }
}