package fop.assignment;

import java.io.File;
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

    // --- VISUALS ---
    private class FloatingText {
        String text; double x, y, life = 1.0; Color color;
        FloatingText(String text, double x, double y, Color color) {
            this.text = text; this.x = x; this.y = y; this.color = color;
        }
    }
    private ArrayList<FloatingText> floatingTexts = new ArrayList<>();

    // --- SETTINGS ---
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

        // 1. Load Data
        loadEnemiesToPool(); 
        
        // 2. Setup Player
        setupPlayer();

        // 3. Initial Spawn
        spawnNextEnemy();    

        // 4. UI Setup
        if (characterMenu != null) characterMenu.setVisible(false);
        if (arenaMenu != null) arenaMenu.setVisible(true);

        // 5. Start Game Loop
        model.loadArena1(); 
        draw();
        startTaskTimer();
        gameStarted = true; 
        
        // --- SOUND: Start Background Music ---
        // Make sure "bgm.mp3" is in src/main/resources/fop/assignment/sounds/
        SoundManager.playMusic("bgm.mp3");
    }

    private void setupPlayer() {
        if (App.globalPlayer != null) {
            this.player = App.globalPlayer;
            this.player.setPosition(20, 20);
            
            // Resurrection Logic: Reset lives if coming from Game Over
            if (!this.player.isAlive()) {
                this.player.lives = 3.0; 
                this.player.isAlive = true; 
            }
        } else {
            // Debug Fallback
            this.player = new Player("Tron", "#00FFFF", 3.0, 1.5);
            this.player.setPosition(20, 20);
            App.globalPlayer = this.player; 
        }
        
        // Reset Logic Flags
        playerTrail.clear(); 
        gameOverTriggered = false;
        currentDir = Direction.NONE;
    }

    // --- MENU ACTIONS ---
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

    // --- GAME LOOP ---
    private void startTaskTimer() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
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
        // --- HORDE SPAWNER ---
        // Keeps spawning enemies until the max limit for the level is reached
        if (activeEnemies.isEmpty() || activeEnemies.size() < getMaxEnemiesForLevel()) {
            spawnNextEnemy();
            if (activeEnemies.isEmpty()) return; // Safety check
        }

        // --- DEATH CHECK ---
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

        // 2. Discs Logic
        for (int i = 0; i < discs.size(); i++) {
            Disc d = discs.get(i);
            d.update(); 
            if (!d.isActive()) {
                discs.remove(i);
                i--;
            }
        }

        // 3. Enemy Logic
        while (enemyTrails.size() < activeEnemies.size()) enemyTrails.add(new LinkedList<>());

        for (int i = 0; i < activeEnemies.size(); i++) {
            Enemy e = activeEnemies.get(i);
            Queue<int[]> eTrail = enemyTrails.get(i);
            
            if (!e.isAlive()) {
                handleEnemyDeath(e, i);
                i--;
                continue;
            }

            // AI Movement
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

            // --- HIT DETECTION ---
            for (Disc d : discs) {
                if (d.isActive() && d.getX() == e.getX() && d.getY() == e.getY()) {
                    // SOUND: Play Hit Sound
                    SoundManager.playSound("hit.wav");
                    
                    e.reduceLives(1.0); 
                    d.deactivate();     
                    spawnFloatingText("HIT!", e.getX(), e.getY(), Color.RED);
                    showMessage("HIT! HP: " + (int)e.getLives());
                }
            }
        }
    }

    // --- GAME OVER ---
    private void triggerGameOverSequence() {
        gameOverTriggered = true;
        gameStarted = false; 
        System.out.println("Player Died. Triggering Game Over Sequence.");

        // SOUND: Stop Music and Play Game Over Sound
        SoundManager.stopMusic();
        SoundManager.playSound("gameover.wav");

        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> {
            try {
                App.setRoot("GameOverPage");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        pause.play();
    }

    // --- ENEMY DEATH & REWARDS ---
    private void handleEnemyDeath(Enemy e, int index) {
        int oldLevel = player.getLevel();
        player.addXP(e.getXPReward());
        int newLevel = player.getLevel();
        
        spawnFloatingText("+" + e.getXPReward() + " XP", player.getX(), player.getY(), Color.GOLD);
        showMessage("DEFEATED " + e.getName());

        if (newLevel > oldLevel) {
            // Optional: Play Level Up Sound
            SoundManager.playSound("levelup.wav");
            checkStoryProgression(newLevel);
        }

        activeEnemies.remove(index);
        enemyTrails.remove(index);
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

    // --- SPAWN LOGIC (HORDE MODE) ---
    private int getMaxEnemiesForLevel() {
        int pLevel = player.getLevel();
        if (pLevel >= 20) return 3; // Chaos Mode
        if (pLevel >= 5) return 2;  // Double Trouble
        return 1;                   // Duel Mode
    }

    private void spawnNextEnemy() {
        // Stop if we have enough enemies
        if (activeEnemies.size() >= getMaxEnemiesForLevel()) return; 

        int playerLevel = player.getLevel();
        String targetName = "Koura"; 

        if (playerLevel < 5) targetName = "Koura";       // Easy
        else if (playerLevel < 10) targetName = "Sark";  // Medium
        else if (playerLevel < 20) targetName = "Rinzler"; // Hard
        else targetName = "Clu";                         // Impossible

        Enemy candidate = null;
        for (Enemy e : enemyPool) {
            if (e.getName().equalsIgnoreCase(targetName)) {
                candidate = e;
                break;
            }
        }

        // Fallbacks
        if (candidate == null && !enemyPool.isEmpty()) candidate = enemyPool.get(0); 
        if (candidate == null) candidate = new Enemy("Default Drone", "#FF0000", 3.0, 1.0, "Easy", 100, "Normal");

        if (candidate != null) {
            // Buff enemies at high levels (> 30)
            double buffSpeed = (playerLevel > 30) ? 0.5 : 0.0;
            double buffLives = (playerLevel > 30) ? (playerLevel - 30) * 0.5 : 0.0;

            // Create fresh copy
            Enemy spawned = new Enemy(candidate.getName(), candidate.getColor(), 
                                      candidate.getLives() + buffLives, 
                                      candidate.getSpeed() + buffSpeed, 
                                      "Normal", candidate.getXPReward(), "Normal");
            
            // Randomize spawn position (Range 5-35)
            int sx = 5 + (int)(Math.random() * 30);
            int sy = 5 + (int)(Math.random() * 30);
            
            // Prevent spawning on player
            if (Math.abs(sx - player.getX()) < 5) sx = 35;

            spawned.setPosition(sx, sy); 
            activeEnemies.add(spawned);
            
            // Only announce new waves
            if (activeEnemies.size() == 1) {
                showMessage("WAVE INCOMING: " + spawned.getName());
            }
        }
    }

    // --- CONTROLS ---
    @FXML
    public void handleKeyPress(KeyEvent event) {
        if (!gameStarted || gameOverTriggered) return; 
        if (activeEnemies.isEmpty()) return;

        if (event.getCode() == KeyCode.SPACE) {
            // SOUND: Play Shoot Sound
            SoundManager.playSound("shoot.wav");
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

    // --- DRAWING ---
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
                    // Quick Parse: Name, Color, Diff, XP, Speed, Intel
                    String name = data[0].trim();
                    String color = "#FF0000"; // Default
                    if (data[1].trim().equalsIgnoreCase("Gold")) color = "#FFD700";
                    else if (data[1].trim().equalsIgnoreCase("Green")) color = "#00FF00";
                    
                    int xp = Integer.parseInt(data[3].trim());
                    double speed = Double.parseDouble(data[4].trim());
                    
                    enemyPool.add(new Enemy(name, color, 3.0, speed, data[2].trim(), xp, data[5].trim()));
                }
            }
            scanner.close();
        } catch (Exception e) { e.printStackTrace(); }
    }
}