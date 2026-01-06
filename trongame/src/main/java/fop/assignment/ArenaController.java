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
    private AnimationTimer gameTimer;
    private ArrayList<Enemy> activeEnemies = new ArrayList<>();
    private ArrayList<Enemy> enemyPool = new ArrayList<>();
    private ArrayList<Disc> discs = new ArrayList<>();
    
    // Input Buffering (Fix for "Ghost Input")
    private Direction bufferedDir = Direction.NONE;

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
    
    // SLOWED DOWN SPEED (0.15s per frame)
    private long speedNanos = 150_000_000;
    
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

        // 3. Load Level Arena
        loadLevelArena(); 

        // 4. Spawn Initial Enemies
        spawnNextEnemy();    

        // 5. Hide Menus
        if (characterMenu != null) characterMenu.setVisible(false);
        if (arenaMenu != null) arenaMenu.setVisible(false);

        // 6. Start Game Loop
        draw();
        startTaskTimer();
        gameStarted = true; 
        SoundManager.playMusic("bgm.mp3");

        // --- INPUT FIX (STEAL FOCUS) ---
        gameCanvas.setFocusTraversable(true);
        
        // We wait for the scene to be ready, then we "Steal" the key listener back
        javafx.application.Platform.runLater(() -> {
            gameCanvas.requestFocus();
            
            // CRITICAL: Overwrite the "Cutscene" listener with the "Game" listener
            if (gameCanvas.getScene() != null) {
                gameCanvas.getScene().setOnKeyPressed(this::handleKeyPress);
            }
        });
    }

    private void setupPlayer() {
        // 1. Load Player Data
        if (App.globalPlayer != null) {
            this.player = App.globalPlayer;
        } else {
            this.player = new Player("Tron", "#00FFFF", 3.0, 1.5);
        }

        // 2. Set Position & Restore Lives
        int startX = 20;
        int startY = 20;
        player.setPosition(startX, startY);
        player.setLives(player.getLives() <= 0 ? 3 : player.getLives()); 

        // --- SAFE SPAWN ZONE ---
        // Clear walls/trails around spawn so you don't instantly die
        for (int i = startX - 2; i <= startX + 2; i++) {
            for (int j = startY - 2; j <= startY + 2; j++) {
                if (i >= 0 && i < 40 && j >= 0 && j < 40) {
                    model.getGrid()[i][j] = 0; 
                }
            }
        }
        // Move enemies away from spawn (Legacy check, safer spawn logic is in spawnNextEnemy now)
        for (Enemy e : activeEnemies) {
            if (Math.abs(e.getX() - startX) < 5 && Math.abs(e.getY() - startY) < 5) {
                e.setPosition(5, 5); 
            }
        }
    }

    private void loadLevelArena() {
        int level = player.getLevel();
        if (level < 10) model.loadArena1();
        else if (level < 20) model.loadArena2();
        else if (level < 30) model.loadArena3();
        else model.loadRandomArena();
    }

    // --- GAME LOOP ---
    private void startTaskTimer() {
        gameTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!gameOverTriggered) {
                    
                    // 1. GAME LOGIC (Throttled - Controls Game Speed)
                    long currentDelay = model.isSpeedBoostActive() ? speedNanos / 2 : speedNanos;
                    
                    if (now - lastUpdate >= currentDelay) {
                        updateGame(); // Move characters, calculate collisions
                        lastUpdate = now;
                    }
                    
                    // 2. DRAWING (Unthrottled - Runs at full 60 FPS)
                    draw(); 
                }
                
                // Floating text also needs smooth animation
                drawFloatingText(gameCanvas.getGraphicsContext2D());
            }
        };
        gameTimer.start(); 
    }   

    private void updateGame() {
        // --- INPUT BUFFER APPLICATION ---
        // Applies the next move direction just before we calculate movement
        if (bufferedDir != Direction.NONE) {
            currentDir = bufferedDir;
            bufferedDir = Direction.NONE; 
        }

        // --- HORDE SPAWNER ---
        if (activeEnemies.isEmpty() || activeEnemies.size() < getMaxEnemiesForLevel()) {
            spawnNextEnemy();
            if (activeEnemies.isEmpty()) return; 
        }

        // --- DEATH CHECK ---
        if (player == null || !player.isAlive()) {
            if (!gameOverTriggered) triggerGameOverSequence();
            return;
        }

        // ==========================================
        // 1. PLAYER MOVEMENT & COLLISION
        // ==========================================
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
            
            // A. FALLING OFF THE GRID (Instant Death)
            if (nextX < 0 || nextX >= 40 || nextY < 0 || nextY >= 40) {
                System.out.println("FATAL: Player fell off the Grid!");
                SoundManager.playSound("fall.wav"); 
                player.reduceLives(player.getLives()); 
                triggerGameOverSequence();
                return;
            }

            // B. WALL/OBSTACLE COLLISION (-0.5 Lives)
            int nextCell = model.getGrid()[nextX][nextY];
            
            if (nextCell == 1 || nextCell == 2) {
                SoundManager.playSound("crash.wav");
                player.reduceLives(0.5); 
                spawnFloatingText("-0.5 HP", player.getX(), player.getY(), Color.ORANGE);
                currentDir = Direction.NONE; // Stop movement
            } 
            else {
                // C. VALID MOVE
                playerTrail.add(new int[]{nextX, nextY});
                model.processMove(player, nextX, nextY);

                if (playerTrail.size() > MAX_TRAIL_LENGTH) {
                    int[] old = playerTrail.poll();
                    model.getGrid()[old[0]][old[1]] = 0; 
                }
            }
        }

        // ==========================================
        // 2. DISC LOGIC
        // ==========================================
        for (int i = 0; i < discs.size(); i++) {
            Disc d = discs.get(i);
            d.update(model.getGrid()); 

            // A. MAGNETIC PICKUP
            if (d.isStationary() && d.isActive()) {
                if (d.getOwner() == player) {
                    double dist = Math.abs(player.getX() - d.getX()) + Math.abs(player.getY() - d.getY());
                    if (dist <= 1.5) { 
                        SoundManager.playSound("pickup.wav");
                        d.returnToOwner(); 
                    }
                }
            }

            // B. COLLISION
            if (!d.isStationary() && d.isActive()) {
                // Enemy Disc hits Player
                if (d.getOwner() != player) {
                    if (player.getX() == d.getX() && player.getY() == d.getY()) {
                        SoundManager.playSound("hit.wav");
                        player.reduceLives(1.0); 
                        spawnFloatingText("-1 HP", player.getX(), player.getY(), Color.RED);
                        d.returnToOwner(); 
                    }
                }
                // Player Disc hits Enemy
                if (d.getOwner() == player) {
                    for (Enemy e : activeEnemies) {
                        if (e.isAlive() && e.getX() == d.getX() && e.getY() == d.getY()) {
                            SoundManager.playSound("hit.wav");
                            e.reduceLives(1.0);
                            spawnFloatingText("CRIT!", e.getX(), e.getY(), Color.RED);
                            d.returnToOwner(); 
                        }
                    }
                }
            }

            if (!d.isActive()) {
                discs.remove(i);
                i--;
            }
        }

        // ==========================================
        // 3. ENEMY LOGIC
        // ==========================================
        while (enemyTrails.size() < activeEnemies.size()) enemyTrails.add(new LinkedList<>());

        for (int i = 0; i < activeEnemies.size(); i++) {
            Enemy e = activeEnemies.get(i);
            Queue<int[]> eTrail = enemyTrails.get(i);
            
            if (!e.isAlive()) {
                handleEnemyDeath(e, i);
                i--; continue;
            }

            // AI Shooting
            if (e.canShoot()) {
                boolean shoot = false;
                int shotDir = -1;
                if (e.getX() == player.getX()) {
                    int dist = player.getY() - e.getY();
                    if (dist > 0 && dist < 10) { shotDir = 1; shoot = true; }
                    else if (dist < 0 && dist > -10) { shotDir = 0; shoot = true; }
                }
                else if (e.getY() == player.getY()) {
                    int dist = player.getX() - e.getX();
                    if (dist > 0 && dist < 10) { shotDir = 3; shoot = true; }
                    else if (dist < 0 && dist > -10) { shotDir = 2; shoot = true; }
                }

                if (shoot) {
                    e.useAmmo(); 
                    discs.add(new Disc(e.getX(), e.getY(), shotDir, e));
                }
            }

            // AI Movement
            int moveDir = e.makeMove(model.getGrid());
            int ex = e.getX();
            int ey = e.getY();
            if (moveDir == 0) ey--; else if (moveDir == 1) ey++; 
            else if (moveDir == 2) ex--; else if (moveDir == 3) ex++; 

            eTrail.add(new int[]{ex, ey});
            model.processMove(e, ex, ey);

            if (eTrail.size() > MAX_TRAIL_LENGTH) {
                int[] old = eTrail.poll();
                model.getGrid()[old[0]][old[1]] = 0;
            }
        }
    }

    // --- GAME OVER ---
    private void triggerGameOverSequence() {
        gameOverTriggered = true;
        gameStarted = false; 
        System.out.println("Player Died. Triggering Game Over Sequence.");

        if (App.globalPlayer != null && App.globalPassword != null) {
            System.out.println("Auto-Saving progress...");
            App.globalPlayer.setLevel(player.getLevel());
            App.globalPlayer.setXP(player.getXP());
            DataManager.savePlayer(App.globalPlayer, App.globalPassword);
        }

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

    // --- ENEMY DEATH & VICTORY CHECK ---
    private void handleEnemyDeath(Enemy e, int index) {
        int oldLevel = player.getLevel();
        
        player.addXP(e.getXPReward()); 
        spawnFloatingText("+" + e.getXPReward() + " XP", player.getX(), player.getY(), Color.GOLD);
        showMessage("DEFEATED " + e.getName());

        int newLevel = player.getLevel();
        if (newLevel > oldLevel) {
            SoundManager.playSound("levelup.wav");
            
            // --- AUTO-SAVE ON LEVEL UP (Leaderboard Fix) ---
            if (App.globalPlayer != null) {
                App.globalPlayer.setLevel(newLevel);
                App.globalPlayer.setXP(player.getXP());
                DataManager.savePlayer(App.globalPlayer, App.globalPassword);
                System.out.println("Level Up! Progress Auto-Saved.");
            }
            // -----------------------------------------------

            // --- 1. VICTORY CHECK (Level 99) ---
            if (newLevel >= 99) {
                triggerGameWinSequence();
                return; // Stop further processing
            }

            // --- 2. Normal Story Check ---
            boolean storyTriggered = checkStoryProgression(newLevel);
            if (!storyTriggered && (newLevel == 20 || newLevel == 30)) {
                triggerArenaUpgradeSequence(newLevel);
            }
        }

        // --- 3. CLEANUP (Erase Enemy Walls) ---
        if (index < activeEnemies.size()) {
            Queue<int[]> deadTrail = enemyTrails.get(index);
            for (int[] pos : deadTrail) {
                model.getGrid()[pos[0]][pos[1]] = 0; 
            }
            activeEnemies.remove(index);
            enemyTrails.remove(index);
        }
    }

    // --- NEW: WIN SEQUENCE ---
    private void triggerGameWinSequence() {
        gameStarted = false;
        if (gameTimer != null) gameTimer.stop();
        
        System.out.println("VICTORY REACHED! Level " + player.getLevel());
        
        // Choose ending based on Character
        String endingChapter = "ending_hero"; // Default (Tron)
        if (player.getName().equalsIgnoreCase("Kevin")) {
            endingChapter = "ending_villain";
        }
        
        final String finalChapter = endingChapter;

        showMessage("SYSTEM LIBERATED!");
        SoundManager.stopMusic();
        SoundManager.playSound("win.wav"); 

        PauseTransition pause = new PauseTransition(Duration.seconds(4));
        pause.setOnFinished(ev -> {
            try {
                // Go to the specific ending cutscene
                App.goToCutscene(finalChapter);
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        pause.play();
    }

    private void triggerArenaUpgradeSequence(int level) {
        gameStarted = false; 
        if (gameTimer != null) gameTimer.stop();
        
        showMessage("ARENA UPGRADE UNLOCKED!");
        SoundManager.playSound("levelup.wav"); 

        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(ev -> {
            try {
                App.setRoot("Arena"); 
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        pause.play();
    }

    private boolean checkStoryProgression(int level) {
        try {
            String nextChapter = null;
            if (level == 10)      nextChapter = "chapter2"; 
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
                if (gameTimer != null) gameTimer.stop(); 
                
                // Save before cutscene
                if (App.globalPlayer != null && App.globalPassword != null) {
                    System.out.println("Story Event: Saving Progress...");
                    App.globalPlayer.setLevel(player.getLevel());
                    App.globalPlayer.setXP(player.getXP());
                    DataManager.savePlayer(App.globalPlayer, App.globalPassword);
                }

                App.goToCutscene(nextChapter);
                return true; 
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return false; 
    }

    private int getMaxEnemiesForLevel() {
        int pLevel = player.getLevel();
        if (pLevel >= 20) return 3; 
        if (pLevel >= 5) return 2;  
        return 1;                   
    }

    private void spawnNextEnemy() {
        if (activeEnemies.size() >= getMaxEnemiesForLevel()) return; 

        int playerLevel = player.getLevel();
        String targetName = "Koura"; 

        if (playerLevel < 5) targetName = "Koura";       
        else if (playerLevel < 10) targetName = "Sark";  
        else if (playerLevel < 20) targetName = "Rinzler"; 
        else targetName = "Clu";                        

        Enemy candidate = null;
        for (Enemy e : enemyPool) {
            if (e.getName().equalsIgnoreCase(targetName)) {
                candidate = e;
                break;
            }
        }
        if (candidate == null && !enemyPool.isEmpty()) candidate = enemyPool.get(0); 
        if (candidate == null) candidate = new Enemy("Default Drone", "#FF0000", 3.0, 1.0, "Easy", 100, "Normal");

        if (candidate != null) {
            double buffSpeed = (playerLevel > 30) ? 0.5 : 0.0;
            double buffLives = (playerLevel > 30) ? (playerLevel - 30) * 0.5 : 0.0;

            Enemy spawned = new Enemy(candidate.getName(), candidate.getColor(), 
                                      candidate.getLives() + buffLives, 
                                      candidate.getSpeed() + buffSpeed, 
                                      "Normal", candidate.getXPReward(), "Normal");
            
            // --- UPDATED SPAWN LOGIC (SAFETY LOOP) ---
            int sx, sy;
            int attempts = 0;
            
            do {
                sx = 1 + (int)(Math.random() * 38); 
                sy = 1 + (int)(Math.random() * 38);
                attempts++;
            } while (attempts < 100 && (
                     model.getGrid()[sx][sy] != 0 || 
                     (Math.abs(sx - player.getX()) < 8 && Math.abs(sy - player.getY()) < 8)
            ));

            spawned.setPosition(sx, sy); 
            activeEnemies.add(spawned);
            if (activeEnemies.size() == 1) showMessage("WAVE INCOMING: " + spawned.getName());
        }
    }

    @FXML
    public void handleKeyPress(KeyEvent event) {
        if (!gameStarted || gameOverTriggered) return; 
        
        if (event.getCode() == KeyCode.SPACE) {
            if (player.hasAmmo()) { 
                SoundManager.playSound("shoot.wav");
                player.useAmmo();   
                discs.add(new Disc(player.getX(), player.getY(), lastFacingDir, player));
            } else {
                System.out.println("NO DISC AMMO!"); 
            }
            return;
        }

        switch (event.getCode()) {
            // --- UPDATED MOVEMENT (Saves to BUFFER) ---
            case W: if (currentDir != Direction.DOWN) bufferedDir = Direction.UP; break;
            case S: if (currentDir != Direction.UP)   bufferedDir = Direction.DOWN; break;
            case A: if (currentDir != Direction.RIGHT) bufferedDir = Direction.LEFT; break;
            case D: if (currentDir != Direction.LEFT)  bufferedDir = Direction.RIGHT; break;
            
            case L: // CHEAT
                int oldCheatLevel = player.getLevel();
                player.addXP(5000); 
                System.out.println("CHEAT: Level is now " + player.getLevel());
                if (player.getLevel() > oldCheatLevel) {
                    if (player.getLevel() >= 99) triggerGameWinSequence(); 
                    else checkStoryProgression(player.getLevel()); 
                }
                break;
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
                if (grid[x][y] == 1) { 
                    gc.setFill(Color.web("#333333")); 
                    gc.fillRect(x*CELL, y*CELL, CELL-1, CELL-1); 
                } 
                else if (grid[x][y] == 2) { 
                    gc.setFill(Color.web("#00FFFF", 0.3)); // Glow
                    gc.fillRect(x*CELL - 2, y*CELL - 2, CELL+4, CELL+4);
                    gc.setFill(Color.web("#00FFFF")); 
                    gc.fillRect(x*CELL, y*CELL, CELL-1, CELL-1); 
                } 
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
            String ammoStr = "DISC: " + player.getCurrentAmmo() + "/" + player.getMaxAmmo();
            String stats = String.format("TRON | LVL: %d | XP: %d | LIVES: %.1f | %s", 
                                         player.getLevel(), player.getXP(), displayLives, ammoStr);
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
                    String color = "#FF0000"; 
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