package fop.assignment;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class ArenaController {
    @FXML private VBox characterMenu, arenaMenu;
    @FXML private Canvas gameCanvas;

    private Character selectedChar;
    private ArenaModel model = new ArenaModel();
    private int playerX = 20, playerY = 20;
    private final int CELL = 15;
    private String currentDir = "NONE";
    private boolean gameStarted = false;
    private long lastUpdate = 0;

    public void initialize() {
        this.selectedChar = (App.chosenCharacter != null) ? App.chosenCharacter : new Tron();
        if (selectedChar instanceof Tron && App.chosenCharacter == null) selectedChar.loadAttributes("Tron");

        if (characterMenu != null) characterMenu.setVisible(false);
        if (arenaMenu != null) arenaMenu.setVisible(true);

        model.loadArena1();
        draw();
        startTaskTimer();
    }

    private void startTaskTimer() {
        new AnimationTimer() {
            public void handle(long now) {
                if (gameStarted) {
                    long delay = (long) (100_000_000 / selectedChar.getSpeed());
                    if (model.isSpeedBoostActive()) delay /= 2;

                    if (now - lastUpdate >= delay) {
                        updateGame();
                        draw();
                        lastUpdate = now;
                    }
                }
            }
        }.start();
    }

    private void updateGame() {
        if (currentDir.equals("NONE") || model.getPlayerLives() <= 0) return;

        int[] next = GameEngine.getNextPosition(playerX, playerY, currentDir);
        model.processMove(next[0], next[1], playerX, playerY);

        if (GameEngine.isWithinBounds(next[0], next[1])) {
            playerX = next[0];
            playerY = next[1];
        }
    }

    @FXML
    public void handleKeyPress(KeyEvent event) {
        if (!gameStarted) return;
        switch (event.getCode()) {
            case W: if (!currentDir.equals("DOWN")) currentDir = "UP"; break;
            case S: if (!currentDir.equals("UP"))   currentDir = "DOWN"; break;
            case A: if (!currentDir.equals("RIGHT")) currentDir = "LEFT"; break;
            case D: if (!currentDir.equals("LEFT"))  currentDir = "RIGHT"; break;
            default: break;
        }
    }

    private void draw() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 600, 600);

        // Draw Grid
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

        // Draw Player and HUD
        String pColor = (model.getPlayerLives() <= 0) ? "#FF0000" : selectedChar.getColor();
        gc.setFill(Color.web(pColor == null ? "#00FF00" : pColor));
        gc.fillRect(playerX * CELL, playerY * CELL, CELL - 1, CELL - 1);

        HUDDisplay.draw(gc, (int)model.getPlayerLives(), selectedChar.getColor());

        if (model.getPlayerLives() <= 0) {
            gameStarted = false;
            arenaMenu.setVisible(true);
            HUDDisplay.drawGameOver(gc);
        }
    }

    @FXML private void loadArena1Action() { model.loadArena1(); startGameSession(); }
    @FXML private void loadArena2Action() { model.loadArena2(); startGameSession(); }
    @FXML private void loadArena3Action() { model.loadArena3(); startGameSession(); }
    @FXML private void loadRandomArenaAction() { model.loadRandomArena(); startGameSession(); }

    private void startGameSession() {
        playerX = 20; playerY = 20; currentDir = "NONE";
        gameStarted = true;
        arenaMenu.setVisible(false);
        gameCanvas.requestFocus();
        draw();
    }
}