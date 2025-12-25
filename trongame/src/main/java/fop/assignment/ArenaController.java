package fop.assignment;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

public class ArenaController {
    // FXML UI Components
    @FXML private VBox characterMenu; 
    @FXML private VBox arenaMenu;     
    @FXML private VBox gameOverMenu; 
    @FXML private Canvas gameCanvas;

    // Game Objects and State
    private Character selectedChar;
    private final ArenaModel model = new ArenaModel();
    private final ArenaRenderer renderer = new ArenaRenderer(); // Specialized Drawing Class
    
    private int playerX = 20;
    private int playerY = 20;
    
    private enum Direction { UP, DOWN, LEFT, RIGHT, NONE }
    private Direction currentDir = Direction.NONE;
    
    private long lastUpdate = 0;
    private final long speedNanos = 200_000_000; // Slower, more manageable speed
    private boolean gameStarted = false;
    private int currentArenaChoice = 1; 

    @FXML
    public void initialize() {
        // Load Character selection from global app state
        if (App.chosenCharacter != null) {
            this.selectedChar = App.chosenCharacter;
        } else {
            this.selectedChar = new Tron();
            this.selectedChar.loadAttributes("Tron");
        }

        // Initial UI Setup
        if (characterMenu != null) characterMenu.setVisible(false);
        if (arenaMenu != null) arenaMenu.setVisible(true);
        
        if (gameOverMenu != null) {
            gameOverMenu.setVisible(false);
            gameOverMenu.setManaged(false); // Prevents layout shifting in StackPane
        }

        model.loadArena1(); 
        draw();
        startTaskTimer();
    }

    // --- Arena Selection Actions ---
    @FXML
    private void loadArena1Action() {
        currentArenaChoice = 1;
        model.loadArena1();
        startGameSession();
    }

    @FXML
    private void loadArena2Action() {
        currentArenaChoice = 2;
        model.loadArena2();
        startGameSession();
    }

    @FXML
    private void loadArena3Action() {
        currentArenaChoice = 3;
        model.loadArena3();
        startGameSession();
    }

    @FXML
    private void loadRandomArenaAction() {
        currentArenaChoice = (int) (Math.random() * 3) + 1;
        model.loadRandomArena();
        startGameSession();
    } 

    // --- Game State Control ---
    @FXML
    private void handleRestart() {
        if (gameOverMenu != null) {
            gameOverMenu.setVisible(false);
            gameOverMenu.setManaged(false);
        }
        
        // Reload the specific arena to reset lives and clear jetwalls
        model.resetLivesAndGrid(currentArenaChoice);

        resetPlayer();
        gameStarted = true;
        gameCanvas.requestFocus(); 
        draw();
    }   

    private void startGameSession() {
        resetPlayer();
        gameStarted = true;
        
        if (arenaMenu != null) arenaMenu.setVisible(false);
        if (gameOverMenu != null) {
            gameOverMenu.setVisible(false);
            gameOverMenu.setManaged(false);
        }
        
        gameCanvas.requestFocus(); 
        draw();
    }

    private void startTaskTimer() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameStarted) {
                    // Calculate speed based on character attributes
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
        // Death Check
        if (model.getPlayerLives() <= 0) {
            gameStarted = false;
            if (gameOverMenu != null) {
                gameOverMenu.setVisible(true);
                gameOverMenu.setManaged(true);
                gameOverMenu.toFront(); // Ensure menu sits on top of StackPane
            }
            return;
        }

        if (currentDir == Direction.NONE) return;

        int nextX = playerX;
        int nextY = playerY;

        switch (currentDir) {
        case UP:    nextY--; break;
        case DOWN:  nextY++; break;
        case LEFT:  nextX--; break;
        case RIGHT: nextX++; break;
        case NONE:  break; // Add this line to satisfy the compiler
        default:    break; // Good practice to include this as well
    }

        model.processMove(nextX, nextY, playerX, playerY);

        // Update player coordinates if within bounds
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
        default: break; // This ignores every other key on the keyboard
    }
    }

    private void resetPlayer() {
        playerX = 20;
        playerY = 20;
        currentDir = Direction.NONE;
    }

    private void draw() {
        // Delegate all drawing tasks to the Renderer class
        renderer.render(gameCanvas.getGraphicsContext2D(), model, selectedChar, playerX, playerY);
    }
}