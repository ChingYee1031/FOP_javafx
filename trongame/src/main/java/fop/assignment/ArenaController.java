package fop.assignment;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class ArenaController {

    // --- UI LINKS ---
    @FXML private VBox characterMenu; 
    @FXML private VBox arenaMenu;     
    @FXML private Canvas gameCanvas;

    // --- GAME DATA ---
    private ArenaModel model = new ArenaModel();
    private Player player;
    private ArrayList<Enemy> activeEnemies = new ArrayList<>();
    private ArrayList<Enemy> enemyPool = new ArrayList<>();
    private ArrayList<Disc> discs = new ArrayList<>();

    // --- TRAIL DECAY SYSTEM ---
    private Queue<int[]> playerTrail = new LinkedList<>();
    private ArrayList<Queue<int[]>> enemyTrails = new ArrayList<>();
    private final int MAX_TRAIL_LENGTH = 50; 

    // --- FLOATING TEXT SYSTEM ---
    private class FloatingText {
        String text;
        double x, y;
        double life = 1.0; 
        Color color;

        FloatingText(String text, double x, double y, Color color) {
            this.text = text; this.x = x; this.y = y; this.color = color;
        }
    }
    private ArrayList<FloatingText> floatingTexts = new ArrayList<>();

    // --- GRAPHICS SETTINGS ---
    private final int CELL = 20; 

    private enum Direction { UP, DOWN, LEFT, RIGHT, NONE }
    private Direction currentDir = Direction.NONE;
    private int lastFacingDir = 0; 

    private long lastUpdate = 0;
    private long speedNanos = 80_000_000; 
    
    // --- STATE FLAGS ---
    private boolean gameStarted = false;
    private boolean gameOverTriggered = false;

    // --- MESSAGES ---
    private String gameMessage = ""; 
    private long messageTimer = 0;

    @FXML
    public void initialize() {
        gameCanvas.setWidth(40 * CELL);
        gameCanvas.setHeight(40 * CELL);

        // 1. Load Data FIRST
        loadEnemiesToPool(); 
        
        // 2. Setup Player (This now fixes the infinite game over loop)
        setupPlayer();

        // 3. Spawn Enemy
        spawnNextEnemy();    

        if (characterMenu != null) characterMenu.setVisible(false);
        if (arenaMenu != null) arenaMenu.setVisible(true);

        model.loadArena1(); 
        
        // 4. Force a Draw immediately so the screen isn't blank
        draw();
        
        // 5. Start the loop
        startTaskTimer();
        
        // 6. FIX: Allow movement immediately, flags are reset in setupPlayer
        gameStarted = true; 
    }

    private void setupPlayer() {
        if (App.globalPlayer != null) {
            this.player = App.globalPlayer;
            this.player.setPosition(20, 20);
            
            // --- FIX 1: RESURRECTION LOGIC ---
            // If the player is dead (lives <= 0), we MUST reset them.
            // Otherwise the game loop sees they are dead and triggers Game Over instantly.
            if (!this.player.isAlive()) {
                this.player.lives = 3.0; // Reset to 3 lives
                this.player.isAlive = true; // Mark as alive
                System.out.println("Player Resurrected!");
            }
        } else {
            // New Game / Debug Fallback
            this.player = new Player("Tron", "#00FFFF", 3.0, 1.5);
            this.player.setPosition(20, 20);
            App.globalPlayer = this.player; 
        }
        
        // Reset Logic Flags
        playerTrail.clear(); 
        gameOverTriggered = false;
        currentDir = Direction.NONE;
    }

    @FXML private void loadArena1Action() { model.loadArena1(); startGameSession(); }
    @FXML private void loadArena2Action() { model.loadArena2(); startGameSession(); }
    @FXML private void loadArena3Action() { model.loadArena3(); startGameSession(); }
    @FXML private void loadRandomArenaAction() { model.loadRandomArena(); startGameSession(); }

    private void startGameSession() {
        currentDir = Direction.NONE;
        gameStarted = true;
        gameOverTriggered = false;
        playerTrail.clear();
        enemyTrails.clear();
        for (int i=0; i<activeEnemies.size(); i++) enemyTrails.add(new LinkedList<>());
        
        if (characterMenu != null) characterMenu.setVisible(false);
        if (arenaMenu != null) arenaMenu.setVisible(false);
        gameCanvas.requestFocus(); 
        draw();
    }

    private void startTaskTimer() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                // If game is over, we stop updating logic, but we still draw (for fading text)
                if (!gameOverTriggered) {
                    long currentDelay = model.isSpeedBoostActive() ? speedNanos / 2 : speedNanos;
                    if (now - lastUpdate >= currentDelay) {
                        updateGame();
                        draw();
                        lastUpdate = now;
                    }
                }
                drawFloatingText(gameCanvas.getGraphicsContext2D());
            }
        }.start();
    }   

    private void updateGame() {
        // --- FIX 2: PREVENT UPDATES IF NO ENEMY YET ---
        // This ensures the game doesn't run logic if the enemy spawn glitched
        if (activeEnemies.isEmpty()) {
            spawnNextEnemy();
            // If still empty after trying to spawn, skip this frame
            if (activeEnemies.isEmpty()) return; 
        }

        // Check Death
        if (player == null || !player.isAlive()) {
            if (!gameOverTriggered) {
                triggerGameOverSequence();
            }
            return;
        }

        // 1. Player Move
        int nextX = player.getX();
        int nextY = player.getY();

        switch (currentDir) {
            case UP:    nextY--; lastFacingDir = 0; break;
            case DOWN:  nextY++; lastFacingDir = 1; break;
            case LEFT:  nextX--; lastFacingDir = 2; break;
            case RIGHT: nextX++; lastFacingDir = 3; break;
            default:    break; 
        }

        if (currentDir != Direction.NONE) {
            playerTrail.add(new int[]{nextX, nextY});
            model.processMove(player, nextX, nextY);

            if (playerTrail.size() > MAX_TRAIL_LENGTH) {
                int[] old = playerTrail.poll();
                model.getGrid()[old[0]][old[1]] = 0; 
            }
        }

        // 2. Discs
        for (int i = 0; i < discs.size(); i++) {
            Disc d = discs.get(i);
            d.update(); 
            if (!d.isActive()) {
                discs.remove(i);
                i--;
            }
        }

        // 3. Enemies
        while (enemyTrails.size() < activeEnemies.size()) enemyTrails.add(new LinkedList<>());

        for (int i = 0; i < activeEnemies.size(); i++) {
            Enemy e = activeEnemies.get(i);
            Queue<int[]> eTrail = enemyTrails.get(i);
            
            if (!e.isAlive()) {
                handleEnemyDeath(e, i);
                i--;
                continue;
            }

            // AI Move
            int moveDir = e.makeMove(model.getGrid());
            int ex = e.getX();
            int ey = e.getY();
            if (moveDir == 0) ey--;      
            else if (moveDir == 1) ey++; 
            else if (moveDir == 2) ex--; 
            else if (moveDir == 3) ex++; 

            eTrail.add(new int[]{ex, ey});
            model.processMove(e, ex, ey);

            if (eTrail.size() > MAX_TRAIL_LENGTH) {
                int[] old = eTrail.poll();
                model.getGrid()[old[0]][old[1]] = 0;
            }

            // Check Hits
            for (Disc d : discs) {
                if (d.isActive() && d.getX() == e.getX() && d.getY() == e.getY()) {
                    e.reduceLives(1.0); 
                    d.deactivate();     
                    spawnFloatingText("HIT!", e.getX(), e.getY(), Color.RED);
                    showMessage("HIT! HP: " + (int)e.getLives());
                }
            }
        }
    }

    private void triggerGameOverSequence() {
        gameOverTriggered = true;
        gameStarted = false; 
        System.out.println("Player Died. Triggering Game Over Sequence.");

        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            try {
                App.setRoot("GameOverPage");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        pause.play();
    }

    private void handleEnemyDeath(Enemy e, int index) {
        int oldLevel = player.getLevel();
        player.addXP(e.getXPReward());
        int newLevel = player.getLevel();
        
        spawnFloatingText("+" + e.getXPReward() + " XP", player.getX(), player.getY(), Color.GOLD);
        showMessage("DEFEATED " + e.getName());

        if (newLevel > oldLevel) {
            checkStoryProgression(newLevel);
        }

        activeEnemies.remove(index);
        enemyTrails.remove(index);
        spawnNextEnemy(); 
    }

    private void checkStoryProgression(int level) {
        try {
            String nextChapter = null;
            if (level == 10) nextChapter = "chapter2";
            else if (level == 19) nextChapter = "chapter3";
            else if (level == 28) nextChapter = "chapter4";
            else if (level == 37) nextChapter = "chapter5";
            else if (level == 46) nextChapter = "chapter6";
            else if (level == 55) nextChapter = "chapter7";
            else if (level == 64) nextChapter = "chapter8";
            else if (level == 73) nextChapter = "chapter9";
            else if (level == 82) nextChapter = "chapter10";

            if (nextChapter != null) {
                gameStarted = false; 
                App.goToCutscene(nextChapter);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void spawnNextEnemy() {
        if (!activeEnemies.isEmpty()) return; 

        int playerLevel = player.getLevel();
        String targetName = "Koura"; 

        if (playerLevel >= 10) targetName = "Clu";
        else if (playerLevel >= 6) targetName = "Rinzler";
        else if (playerLevel >= 3) targetName = "Sark";

        Enemy candidate = null;
        for (Enemy e : enemyPool) {
            if (e.getName().equalsIgnoreCase(targetName)) {
                candidate = new Enemy(e.getName(), e.getColor(), e.getLives(), e.getSpeed(), "Normal", e.getXPReward(), "Normal");
                break;
            }
        }

        // --- FIX 3: SAFE FALLBACK ---
        // If we couldn't find the specific enemy (e.g. "Clu" not in text file), pick the first one available.
        if (candidate == null && !enemyPool.isEmpty()) candidate = enemyPool.get(0); 

        // If the file was empty or missing, create a dummy default enemy
        if (candidate == null) {
            candidate = new Enemy("Default Drone", "#FF0000", 3.0, 1.0, "Easy", 100, "Normal");
        }

        if (candidate != null) {
            // Create a COPY of the enemy for the arena (so we don't modify the pool version)
            Enemy spawned = new Enemy(candidate.getName(), candidate.getColor(), candidate.getLives(), candidate.getSpeed(), "Normal", candidate.getXPReward(), "Normal");
            spawned.setPosition(35, 35); 
            activeEnemies.add(spawned);
            showMessage("NEW CHALLENGER: " + spawned.getName());
        }
    }

    @FXML
    public void handleKeyPress(KeyEvent event) {
        if (!gameStarted || gameOverTriggered) return; 
        
        // --- FIX 4: MOVEMENT CHECK ---
        // Prevent movement if the enemy hasn't spawned yet
        if (activeEnemies.isEmpty()) return;

        if (event.getCode() == KeyCode.SPACE) {
            discs.add(new Disc(player.getX(), player.getY(), lastFacingDir));
            return;
        }
        switch (event.getCode()) {
            case W: if (currentDir != Direction.DOWN) currentDir = Direction.UP; break;
            case S: if (currentDir != Direction.UP)   currentDir = Direction.DOWN; break;
            case A: if (currentDir != Direction.RIGHT) currentDir = Direction.LEFT; break;
            case D: if (currentDir != Direction.LEFT)  currentDir = Direction.RIGHT; break;
            default: break;
        }
    }

    private void draw() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        int[][] grid = model.getGrid();
        for (int x = 0; x < 40; x++) {
            for (int y = 0; y < 40; y++) {
                if (grid[x][y] == 1) { gc.setFill(Color.web("#333333")); gc.fillRect(x*CELL, y*CELL, CELL-1, CELL-1); } 
                else if (grid[x][y] == 2) { gc.setFill(Color.web("#00FFFF", 0.6)); gc.fillRect(x*CELL, y*CELL, CELL-1, CELL-1); } 
                else if (grid[x][y] == 3) { gc.setFill(Color.YELLOW); gc.fillRect(x*CELL, y*CELL, CELL-1, CELL-1); }
            }
        }

        for (Disc d : discs) d.draw(gc, CELL);
        drawCharacter(gc, player);
        for (Enemy e : activeEnemies) drawCharacter(gc, e);
        drawHUD(gc);
    }

    private void drawCharacter(GraphicsContext gc, GameCharacter c) {
        if (c != null && c.isAlive()) {
            if (c instanceof Enemy && ((Enemy)c).getIcon() != null) {
                double size = CELL * 1.5; 
                double offset = (size - CELL) / 2;
                gc.drawImage(((Enemy)c).getIcon(), (c.getX() * CELL) - offset, (c.getY() * CELL) - offset, size, size);
            } else {
                gc.setFill(Color.web(c.getColor()));
                gc.fillRect(c.getX() * CELL, c.getY() * CELL, CELL - 1, CELL - 1);
            }
        }
    }

    private void drawHUD(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 0, 0, 0.8)); 
        gc.fillRect(0, 0, gameCanvas.getWidth(), 40); 
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        
        if (player != null) {
            double displayLives = Math.max(0, player.getLives());
            String stats = String.format("TRON | LVL: %d | XP: %d | LIVES: %.0f", 
                                         player.getLevel(), player.getXP(), displayLives);
            gc.fillText(stats, 20, 27);
        }

        if (!gameMessage.isEmpty() && (System.nanoTime() - messageTimer) < 2_000_000_000L) {
            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
            gc.fillText(gameMessage, 200, 100);
        }
    }

    private void showMessage(String msg) {
        this.gameMessage = msg;
        this.messageTimer = System.nanoTime();
    }

    private void spawnFloatingText(String text, int gridX, int gridY, Color color) {
        floatingTexts.add(new FloatingText(text, gridX * CELL, gridY * CELL, color));
    }

    private void drawFloatingText(GraphicsContext gc) {
        if (floatingTexts.isEmpty()) return;
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        Iterator<FloatingText> it = floatingTexts.iterator();
        while (it.hasNext()) {
            FloatingText ft = it.next();
            gc.setFill(ft.color);
            gc.setGlobalAlpha(ft.life); 
            gc.fillText(ft.text, ft.x, ft.y);
            ft.y -= 0.5; 
            ft.life -= 0.02; 
            if (ft.life <= 0) it.remove();
        }
        gc.setGlobalAlpha(1.0); 
    }

    private void loadEnemiesToPool() {
        try {
            File file = new File("enemies.txt");
            if (!file.exists()) return;
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",");
                if (data.length >= 6) {
                    String name = data[0].trim();
                    String colorName = data[1].trim();
                    String difficulty = data[2].trim();
                    int xp = Integer.parseInt(data[3].trim());
                    double speed = Double.parseDouble(data[4].trim());
                    String intelligence = data[5].trim();
                    String hexColor = "#FFFFFF";
                    if (colorName.equalsIgnoreCase("Gold")) hexColor = "#FFD700";
                    else if (colorName.equalsIgnoreCase("Red")) hexColor = "#FF0000";
                    else if (colorName.equalsIgnoreCase("Yellow")) hexColor = "#FFFF00";
                    else if (colorName.equalsIgnoreCase("Green")) hexColor = "#00FF00";
                    enemyPool.add(new Enemy(name, hexColor, 3.0, speed, difficulty, xp, intelligence));
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) { e.printStackTrace(); }
    }
}