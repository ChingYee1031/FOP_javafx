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
// Change this line in your ArenaController
    private CharacterSelection selectedChar;
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
        // 1. Retrieve the character chosen in the previous screen
        if (App.globalSelectedCharacter != null) {
            this.selectedChar = App.globalSelectedCharacter;
        } else {
            // Fallback if someone runs Arena directly without selecting
            // Default selection is tron
            System.out.println("Warning: No character selected in previous screen.");
            this.selectedChar = new Tron(); 
            this.selectedChar.loadAttributes("Tron");
        }

        // 2. Hide the Menu VBox because the choice is already made!
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
                    long currentDelay = model.isSpeedBoostActive() ? speedNanos / 2 : speedNanos;
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
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 600, 600);

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

        // Draw Player Head
        if (model.getPlayerLives() <= 0) {
            gc.setFill(Color.RED);
        } else {
            // Check if a character is actually selected before asking for its color
            if (selectedChar != null) {
                String hexColor = selectedChar.getColor();
                gc.setFill((hexColor == null || hexColor.isEmpty()) ? Color.LIME : Color.web(hexColor));
            } else {
                // If game just loaded and no character is picked yet, default to White
                gc.setFill(Color.WHITE);
            }
        }
        gc.fillRect(playerX * CELL, playerY * CELL, CELL - 1, CELL - 1);
        
        // Handle Death / Reset Screen
       if (model.getPlayerLives() <= 0) {
        gameStarted = false;
    
        // Hide the character selection menu so it's not in the way
        if (characterMenu != null) characterMenu.setVisible(false);
    
        // Show the arena selection menu immediately
        if (arenaMenu != null) arenaMenu.setVisible(true); 
    
        gc.setFill(Color.WHITE);
        gc.fillText("DEREZZED - SELECT AN ARENA TO TRY AGAIN", 180, 300);
        }
    }
 }