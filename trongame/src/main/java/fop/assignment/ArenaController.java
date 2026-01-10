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
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class ArenaController {

    @FXML private Canvas gameCanvas;
    @FXML private VBox pauseMenu; 
    
    // Sidebar Labels
    @FXML private Label nameLabel;
    @FXML private Label levelLabel;
    @FXML private Label xpLabel;
    @FXML private Label livesLabel;
    @FXML private Label discLabel;

    private ArenaModel model = new ArenaModel();
    private Player player;
    private AnimationTimer gameTimer;
    private ArrayList<Enemy> activeEnemies = new ArrayList<>();
    private ArrayList<Enemy> enemyPool = new ArrayList<>();
    private ArrayList<Disc> discs = new ArrayList<>();
    
    private Direction bufferedDir = Direction.NONE;
    private Queue<int[]> playerTrail = new LinkedList<>();
    private ArrayList<Queue<int[]>> enemyTrails = new ArrayList<>();
    private final int MAX_TRAIL_LENGTH = 50; 

    // Visuals
    private class FloatingText {
        String text; double x, y, life = 1.0; Color color;
        FloatingText(String text, double x, double y, Color color) {
            this.text = text; this.x = x; this.y = y; this.color = color;
        }
    }
    private ArrayList<FloatingText> floatingTexts = new ArrayList<>();

    private final int CELL = 18;     
    private final int COLS = 40;     
    private final int ROWS = 40;     
    
    private enum Direction { UP, DOWN, LEFT, RIGHT, NONE }
    private Direction currentDir = Direction.NONE;
    private int lastFacingDir = 0; 

    private long lastUpdate = 0;
    private long speedNanos = 150_000_000;
    
    private boolean gameStarted = false;
    private boolean gameOverTriggered = false;
    private boolean isPaused = false;
    private boolean isEndingSequenceActive = false;

    // --- MESSAGE SYSTEM ---
    private String gameMessage = ""; 
    private long messageTimer = 0;
    private Color messageColor = Color.YELLOW; 

    @FXML
    public void initialize() {
        gameCanvas.setWidth(COLS * CELL);  
        gameCanvas.setHeight(ROWS * CELL); 

        loadEnemiesToPool(); 
        setupPlayer();
        
        // --- MAP LOADING (Aligned with Story) ---
        loadLevelArena(); 
        
        spawnNextEnemy();    

        draw();
        startTaskTimer();
        gameStarted = true; 
        
        SoundManager.playMusic("bgm.mp3");

        // Focus Handling to ensure Keyboard works immediately
        gameCanvas.setFocusTraversable(true);
        javafx.application.Platform.runLater(() -> {
            gameCanvas.requestFocus();
            if (gameCanvas.getScene() != null) {
                gameCanvas.getScene().setOnKeyPressed(this::handleKeyPress);
            }
        });
        
        gameCanvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(this::handleKeyPress);
            }
        });
    }
    
    // --- MAP LOADING LOGIC ---
    private void loadLevelArena() {
        int level = player.getLevel();
        
        // Level 1-9: Arena 1
        if (level < 10) {
            model.loadArena1(); 
            showMessage("ARENA 1: THE GRID");
        } 
        // Level 10-19: Arena 2
        else if (level < 20) {
            model.loadArena2(); 
            showMessage("ARENA 2: ACCELERATION");
        } 
        // Level 20-29: Arena 3 (Easier Version)
        else if (level < 30) {
            model.loadArena3(); 
            showMessage("ARENA 3: THE BUNKER");
        } 
        // Level 30+: Arena 4 (Scales with Level)
        else {
            model.loadRandomArena(level); 
            showMessage("ARENA 4: UNSTABLE GRID", Color.RED);
        }
    }

    // --- STORY TRIGGERS (Matched to Arena Boundaries) ---
    private boolean checkStoryProgression(int level) {
        try {
            String nextChapter = null;
            
            // Triggers exactly when entering a new Arena Tier
            if (level == 10)      nextChapter = "chapter2"; 
            else if (level == 20) nextChapter = "chapter3"; 
            else if (level == 30) nextChapter = "chapter4"; 
            
            // Further progression
            else if (level == 40) nextChapter = "chapter5"; 
            else if (level == 50) nextChapter = "chapter6"; 
            else if (level == 60) nextChapter = "chapter7"; 
            else if (level == 70) nextChapter = "chapter8"; 
            else if (level == 80) nextChapter = "chapter9"; 
            else if (level == 90) nextChapter = "chapter10"; 
            
            if (nextChapter != null) {
                gameStarted = false;
                if (gameTimer != null) gameTimer.stop(); 
                
                // Save before cutscene
                if (App.globalPlayer != null && App.globalPassword != null) {
                    App.globalPlayer.setLevel(player.getLevel());
                    App.globalPlayer.setXP(player.getXP());
                    DataManager.savePlayer(App.globalPlayer, App.globalPassword);
                }
                
                SoundManager.stopMusic();
                App.goToCutscene(nextChapter);
                return true; 
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return false; 
    }

    @FXML
    private void handlePauseButton() {
        if (gameOverTriggered) return; 
        isPaused = true;
        SoundManager.pauseMusic();
        gameTimer.stop(); 
        pauseMenu.setVisible(true); 
    }

    @FXML
    private void handleResume() {
        isPaused = false;
        pauseMenu.setVisible(false); 
        SoundManager.resumeMusic();
        gameTimer.start(); 
        gameCanvas.requestFocus(); 
    }

    @FXML
    private void handleQuitToMenu() {
        SoundManager.stopMusic();
        if (App.globalPlayer != null && App.globalPassword != null) {
            App.globalPlayer.setLevel(player.getLevel());
            App.globalPlayer.setXP(player.getXP());
            DataManager.savePlayer(App.globalPlayer, App.globalPassword);
        }
        try { App.setRoot("MenuPage"); } catch (Exception e) {}
    }

    private void startTaskTimer() {
        gameTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isPaused) return; 
                if (!gameOverTriggered) {
                    model.updateRampRespawns();
                    long currentDelay = model.isSpeedBoostActive() ? speedNanos / 2 : speedNanos;
                    if (now - lastUpdate >= currentDelay) {
                        updateGame(); 
                        lastUpdate = now;
                    }
                    draw(); 
                    updateSidebar(); 
                }
                drawFloatingText(gameCanvas.getGraphicsContext2D());
            }
        };
        gameTimer.start(); 
    }   

    private void updateSidebar() {
        if (player != null) {
            if (nameLabel != null) nameLabel.setText("CHARACTER: " + player.getCharacterModel().toUpperCase());
            if (levelLabel != null) levelLabel.setText("LEVEL: " + player.getLevel());
            if (xpLabel != null) xpLabel.setText("XP: " + player.getXP());
            if (livesLabel != null) livesLabel.setText(String.format("LIVES: %.1f", player.getLives()));
            
            // Show Cooldown status in the Disc Label
            if (player.isCooldownReady()) {
                if (discLabel != null) discLabel.setText("DISC: READY (" + player.getCurrentDiscSlots() + ")");
                if (discLabel != null) discLabel.setTextFill(Color.CYAN);
            } else {
                long time = player.getCooldownRemaining() / 1000;
                if (discLabel != null) discLabel.setText("WAIT: " + (time + 1) + "s");
                if (discLabel != null) discLabel.setTextFill(Color.ORANGE);
            }
        }
    }

private void updateGame() {
        if (bufferedDir != Direction.NONE) {
            currentDir = bufferedDir;
            bufferedDir = Direction.NONE; 
        }

        // Spawn Enemies
        if (activeEnemies.isEmpty() || activeEnemies.size() < getMaxEnemiesForLevel()) {
            spawnNextEnemy();
            if (activeEnemies.isEmpty()) return; 
        }

        // Check Player Life
        if (player == null || !player.isAlive()) {
            if (!gameOverTriggered) triggerGameOverSequence();
            return;
        }

        // --- 1. MOVE PLAYER ---
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
            if (nextX < 0 || nextX >= COLS || nextY < 0 || nextY >= ROWS) {
                if(model.isOpenType()) {
                    SoundManager.playSound("crash.wav"); 
                    player.reduceLives(100); 
                    triggerGameOverSequence();
                    return;
                } else {
                    SoundManager.playSound("crash.wav");
                    player.reduceLives(0.5); 
                    spawnFloatingText("-0.5 HP", player.getX(), player.getY(), Color.ORANGE);
                    currentDir = Direction.NONE; 
                    showMessage("CRASHED!", Color.RED);
                }
            } else {
                int nextCell = model.getGrid()[nextX][nextY];

                //collision check
                if (nextCell == 1 || nextCell == 2 || nextCell == 4) {
                    SoundManager.playSound("crash.wav");
                    player.reduceLives(0.5); 
                    spawnFloatingText("-0.5 HP", player.getX(), player.getY(), Color.ORANGE);
                    currentDir = Direction.NONE; 
                    showMessage("CRASHED!", Color.RED);
                } else {
                    if(nextCell == 3){
                        spawnFloatingText("Speed Boost!", player.getX(), player.getY(), Color.CYAN);
                    }
                    playerTrail.add(new int[]{player.getX(), player.getY()});
                    model.processMove(player, nextX, nextY);
                    if (playerTrail.size() > MAX_TRAIL_LENGTH) {
                        int[] old = playerTrail.poll();
                        model.getGrid()[old[0]][old[1]] = 0; 
                    }
                }
            }
        }

        // --- 2. UPDATE DISCS ---
        for (int i = 0; i < discs.size(); i++) {
            Disc d = discs.get(i);
            
            // --- NEW: DESPAWN CHECK (Enemies Only) ---
            if (d.isActive() && d.isStationary()) {
                // Check if it belongs to an Enemy AND time is up
                if (d.getOwner() instanceof Enemy && d.checkDespawn()) {
                    d.returnToOwner(); // Remove disc, give ammo back to Enemy
                    continue; // Skip to next disc
                }
            }
            // Pickup Logic (Exact tile match required)
            if (d.isActive() && d.isStationary()) {
                if (d.getOwner() == player) {
                    if (player.getX() == d.getX() && player.getY() == d.getY()) {
                        d.returnToOwner(); 
                    }
                }
                continue; 
            }

            // Movement Loop (2x Speed)
            for (int speed = 0; speed < 2; speed++) {
                if (!d.isActive() || d.isStationary()) break;

                d.update(model.getGrid()); 

                if (!d.isStationary() && d.isActive()) {
                    // Hit Player?
                    if (d.getOwner() != player) {
                        if (player.getX() == d.getX() && player.getY() == d.getY()) {
                            SoundManager.playSound("hit.wav");
                            player.reduceLives(1.0); 
                            d.returnToOwner(); 
                        }
                    }
                    // Hit Enemy?
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
            } 
            if (!d.isActive()) { discs.remove(i); i--; }
        }

        // --- 3. UPDATE ENEMIES ---
        while (enemyTrails.size() < activeEnemies.size()) enemyTrails.add(new LinkedList<>());
        
        for (int i = 0; i < activeEnemies.size(); i++) {
            Enemy e = activeEnemies.get(i);
            Queue<int[]> eTrail = enemyTrails.get(i);
            
            if (!e.isAlive()) {
                handleEnemyDeath(e, i);
                i--; continue;
            }
            
            // --- MODIFIED: HEAD-ON COLLISION ---
            if (e.getX() == player.getX() && e.getY() == player.getY()) {
                 SoundManager.playSound("crash.wav");
                 
                 // 1. Kill the Enemy
                 e.reduceLives(100); 
                 
                 // 2. Hurt the Player (Only 0.5 damage now)
                 player.reduceLives(0.5); 
                 spawnFloatingText("-0.5 HP", player.getX(), player.getY(), Color.ORANGE);
                 showMessage("CRASHED!", Color.RED);

                 // 3. Check if Player died from the damage
                 if (!player.isAlive()) {
                     triggerGameOverSequence();
                     return; 
                 }
                 
                 // 4. Skip the rest of this enemy's logic (they are dead)
                 continue; 
            }

            // Shooting
            if (e.canShoot()) {
                boolean shoot = false; int shotDir = -1;
                if (e.getX() == player.getX()) {
                    int dist = player.getY() - e.getY();
                    if (dist > 0 && dist < 15) { shotDir = 1; shoot = true; } 
                    else if (dist < 0 && dist > -15) { shotDir = 0; shoot = true; } 
                }
                else if (e.getY() == player.getY()) {
                    int dist = player.getX() - e.getX();
                    if (dist > 0 && dist < 15) { shotDir = 3; shoot = true; } 
                    else if (dist < 0 && dist > -15) { shotDir = 2; shoot = true; } 
                }
                if (shoot) { e.useDisc(); discs.add(new Disc(e.getX(), e.getY(), shotDir, e)); }
            }
            
            // Get Move Decision from AI
            int moveDir = e.makeMove(model.getGrid());

            // --- TRAP LOGIC START ---
            if (moveDir == -2) {
                // Enemy is trapped! Kill them.
                SoundManager.playSound("crash.wav");
                e.reduceLives(100); 
                spawnFloatingText("TRAPPED!", e.getX(), e.getY(), Color.RED);
                continue; // Skip to next enemy (will be removed next frame)
            }
            // --- TRAP LOGIC END ---

            // Execute Move (Only if 0-3)
            if (moveDir >= 0) {
                int ex = e.getX(); int ey = e.getY();
                if (moveDir == 0) ey--; else if (moveDir == 1) ey++; 
                else if (moveDir == 2) ex--; else if (moveDir == 3) ex++; 

                if (ex >= 0 && ex < COLS && ey >= 0 && ey < ROWS) {
                    eTrail.add(new int[]{e.getX(), e.getY()});
                    model.processMove(e, ex, ey);
                }
                if (eTrail.size() > MAX_TRAIL_LENGTH) {
                    int[] old = eTrail.poll();
                    model.getGrid()[old[0]][old[1]] = 0;
                }
            }
        }
    }

    private void handleEnemyDeath(Enemy e, int index) {
        int oldLevel = player.getLevel();
        
        // 1. Add XP
        player.addXP(e.getXPReward()); 
        spawnFloatingText("+" + e.getXPReward() + " XP", player.getX(), player.getY(), Color.GOLD);
        showMessage("DEFEATED " + e.getName());
        
        int newLevel = player.getLevel();
        
        // 2. Handle Level Up (Visuals & Story)
        if (newLevel > oldLevel) {
            showMessage("LEVEL UP!", Color.GREEN);
            SoundManager.playSound("levelup.wav");
            
            // Refill Life/Ammo on Level Up
            player.levelUp(); 
            
            if (App.globalPlayer != null) {
                App.globalPlayer.setLevel(newLevel);
                App.globalPlayer.setXP(player.getXP());
                DataManager.savePlayer(App.globalPlayer, App.globalPassword);
            }
            
            checkStoryProgression(newLevel);
        }

        // --- 3. WIN CONDITION (MUST BE OUTSIDE THE LEVEL UP BLOCK) ---
        // Requirement: Level 99 AND 10,000 XP currently in the bar.
        if (newLevel >= 99 && player.getXP() >= 10000) { 
            triggerGameWinSequence();
            isEndingSequenceActive = true;
            //removed a return here
        }

        // 4. Cleanup Enemy
        if (index < activeEnemies.size()) {
            Queue<int[]> deadTrail = enemyTrails.get(index);
            for (int[] pos : deadTrail) model.getGrid()[pos[0]][pos[1]] = 0; 
            activeEnemies.remove(index);
            enemyTrails.remove(index);
        }
    }

    private void draw() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        
        // 1. CLEAR SCREEN
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        
        // 2. DRAW GRID LINES
        gc.setStroke(Color.web("#222222"));
        gc.setLineWidth(1);
        for(int x = 0; x <= COLS; x++) gc.strokeLine(x * CELL, 0, x * CELL, ROWS * CELL);
        for(int y = 0; y <= ROWS; y++) gc.strokeLine(0, y * CELL, COLS * CELL, y * CELL);

        // 3. DRAW GRID OBJECTS
        int[][] grid = model.getGrid();
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                if (grid[x][y] == 1) { 
                    gc.setFill(Color.web("#333333")); 
                    gc.fillRect(x*CELL, y*CELL, CELL-1, CELL-1); 
                } 
                else if (grid[x][y] == 3) { 
                    gc.setFill(Color.web("#FFFF00", 0.5)); 
                    gc.fillRect(x*CELL, y*CELL, CELL-1, CELL-1); 
                }
                else if (grid[x][y] == 2) { 
                    gc.setFill(Color.web(player.getColor())); 
                    gc.fillRect(x*CELL, y*CELL, CELL-1, CELL-1); 
                } 
                else if (grid[x][y] == 4) {
                    gc.setFill(Color.RED); 
                    gc.fillRect(x*CELL, y*CELL, CELL-1, CELL-1); 
                }
            }
        }

        // 4. DRAW CHARACTERS (Bottom Layer)
        drawCharacter(gc, player);
        for (Enemy e : activeEnemies) drawCharacter(gc, e);
        
        // 5. DRAW DISCS (Top Layer - Floating Effect)
        for (Disc d : discs) d.draw(gc, CELL);
        
        // 6. DRAW HUD MESSAGE (Top-Center)
        if (!gameMessage.isEmpty() && (System.nanoTime() - messageTimer) < 2_000_000_000L) {
            gc.save(); 
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.TOP); 
            
            gc.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
            
            double centerX = gameCanvas.getWidth() / 2;
            double topY = 60; 
            
            // Shadow
            gc.setFill(Color.BLACK);
            gc.fillText(gameMessage, centerX + 2, topY + 2);

            // Main Text
            gc.setFill(messageColor);
            gc.fillText(gameMessage, centerX, topY);
            
            gc.restore(); 
        }
    }

    private void setupPlayer() {
        if (App.globalPlayer != null) {
            this.player = App.globalPlayer;
        } else {
            this.player = new Player("Tron", "#00FFFF", 3.0, 1.5);
        }
        player.setLives(player.getLives() <= 0 ? 3 : player.getLives()); 
        player.refillDiscSlots();
        
        int startX = 20; int startY = 20;
        player.setPosition(startX, startY);
        for (int i = startX - 2; i <= startX + 2; i++) {
            for (int j = startY - 2; j <= startY + 2; j++) {
                if (i >= 0 && i < COLS && j >= 0 && j < ROWS) {
                    model.getGrid()[i][j] = 0; 
                }
            }
        }
    }

    private void triggerGameOverSequence() {
        gameOverTriggered = true; gameStarted = false; 
        if (App.globalPlayer != null && App.globalPassword != null) {
            App.globalPlayer.setLevel(player.getLevel());
            App.globalPlayer.setXP(player.getXP());
            DataManager.savePlayer(App.globalPlayer, App.globalPassword);
        }
        SoundManager.stopMusic();
        SoundManager.playSound("gameover.wav");
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> { try { App.setRoot("GameOverPage"); } catch (Exception e) {} });
        pause.play();
    }

   private void triggerGameWinSequence() {
        // If this method has run before, STOP immediately.
        if (isEndingSequenceActive) {
            return; 
        }
        // Lock the door immediately so the next call (1/60th sec later) is blocked.
        isEndingSequenceActive = true;
        gameStarted = false; 

        // --- SAFETY FIX START ---
        try {
            updateSidebar(); // We wrap this so if it crashes, the game still finishes!
        } catch (Exception e) {
            System.out.println("Sidebar update failed, but ignoring it.");
        }
        // --- SAFETY FIX END ---

        if (gameTimer != null) gameTimer.stop();

        showMessage("MAXIMUM LEVEL ACHIEVED!", Color.CYAN);
        SoundManager.stopMusic();
        SoundManager.playSound("levelup.wav");
        
        // Save Code...
        //SAVE WITH XP CAP (FIXES 9-DIGIT BUG) ---
        try {
            if (App.globalPlayer != null && App.globalPassword != null) {
                App.globalPlayer.setLevel(99);
                App.globalPlayer.setXP(10000);
                DataManager.savePlayer(App.globalPlayer, App.globalPassword);
            }
        } catch (Exception e) {
            System.out.println("Save failed: " + e.getMessage());
        }

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(ev -> { 
            try { 
                App.setRoot("EndingPage"); 
            } catch (Exception ex) { 
                ex.printStackTrace();
            } 
        });
        pause.play();
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
            if (e.getName().equalsIgnoreCase(targetName)) { candidate = e; break; }
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
            int sx = 2 + (int)(Math.random() * (COLS - 4));
            int sy = 2 + (int)(Math.random() * (ROWS - 4));
            if (Math.abs(sx - player.getX()) < 10 && Math.abs(sy - player.getY()) < 10) {
                sx = (player.getX() > 20) ? 5 : 35; 
                sy = (player.getY() > 20) ? 5 : 35;
            }
            spawned.setPosition(sx, sy); 
            activeEnemies.add(spawned);
            if (activeEnemies.size() == 1) showMessage("WAVE INCOMING: " + spawned.getName());
        }
    }

    @FXML
    public void handleKeyPress(KeyEvent event) {
        if (!gameStarted || gameOverTriggered || isPaused) return; 
        
        if (event.getCode() == KeyCode.SPACE) {
            // Check 1: Ammo
            if (!player.hasDisc()) {
                showMessage("NO DISC REMAIN!", Color.RED);
                return;
            }
            
            // Check 2: Cooldown (5 Seconds)
            if (!player.isCooldownReady()) {
                long timeLeft = player.getCooldownRemaining() / 1000;
                showMessage("COOLDOWN: " + (timeLeft + 1) + "s", Color.ORANGE);
                return;
            }

            // Execute Throw
            SoundManager.playSound("shoot.wav");
            player.useDisc();
            player.resetCooldown(); // Start Timer
            
            // --- FIX: SPAWN DISC 1 BLOCK AHEAD ---
            int spawnX = player.getX();
            int spawnY = player.getY();
            
            switch(lastFacingDir) {
                case 0: spawnY--; break; // UP
                case 1: spawnY++; break; // DOWN
                case 2: spawnX--; break; // LEFT
                case 3: spawnX++; break; // RIGHT
            }
            
            discs.add(new Disc(spawnX, spawnY, lastFacingDir, player));
            return;
        }
        
        switch (event.getCode()) {
            case W: if (currentDir != Direction.DOWN) bufferedDir = Direction.UP; break;
            case S: if (currentDir != Direction.UP)   bufferedDir = Direction.DOWN; break;
            case A: if (currentDir != Direction.RIGHT) bufferedDir = Direction.LEFT; break;
            case D: if (currentDir != Direction.LEFT)  bufferedDir = Direction.RIGHT; break;
            case L: // SAFETY 1: Ignore input if we are already winning
                if (isEndingSequenceActive) return; 

                /* Cheat logic */ 
                int oldCheatLevel = player.getLevel();
                player.addXP(500); 

                if (player.getLevel() > oldCheatLevel) {
                    checkStoryProgression(player.getLevel()); 
                }

                // CHECK WIN
                if (player.getLevel() >= 99 && player.getXP() >= 10000) {
                    // SAFETY 2: Lock immediately so next 'L' press does nothing
                    isEndingSequenceActive = true; 
                    triggerGameWinSequence();
                }
                break;
            default: break;
        }
    }

private void drawCharacter(GraphicsContext gc, GameCharacter c) {
        if (c != null && c.isAlive()) {
            
            // 1. ERASE BACKGROUND FIRST
            // Paints a black square to hide any trail/grid lines directly underneath
            gc.setFill(Color.BLACK);
            gc.fillRect(c.getX() * CELL, c.getY() * CELL, CELL - 1, CELL - 1);

            // 2. SETUP GLOW EFFECT (Only for Player)
            // This separates the "Head" (Glowing) from the "Jetwall" (Flat)
            if (c == player) {
                gc.save(); // Save the current state (clean)
                
                DropShadow glow = new DropShadow();
                glow.setColor(Color.web(c.getColor())); // Glow matches player color
                glow.setRadius(20); // How wide the glow is
                glow.setSpread(0.5); // How intense the glow is
                
                gc.setEffect(glow); // Apply the effect
            }

            // 3. DRAW THE CHARACTER
            if (c instanceof Enemy && ((Enemy)c).getIcon() != null) {
                // Draw Enemy Icon
                double size = CELL * 1.5; 
                double offset = (size - CELL) / 2;
                gc.drawImage(((Enemy)c).getIcon(), (c.getX() * CELL) - offset, (c.getY() * CELL) - offset, size, size);
            } else {
                // Draw Player (The "Head")
                gc.setFill(Color.web(c.getColor()));
                gc.fillRect(c.getX() * CELL, c.getY() * CELL, CELL - 1, CELL - 1);
                
                // OPTIONAL: Add a "White Core" to look like a light source
                // This makes the head look brighter than the trail
                if (c == player) {
                    gc.setFill(Color.WHITE);
                    gc.fillRect((c.getX() * CELL) + 4, (c.getY() * CELL) + 4, CELL - 9, CELL - 9);
                }
            }

            // 4. RESTORE STATE (Turn off glow)
            if (c == player) {
                gc.restore(); // Go back to normal drawing for the next items
            }
        }
    }

    private void showMessage(String msg) { showMessage(msg, Color.YELLOW); }
    private void showMessage(String msg, Color color) {
        this.gameMessage = msg; this.messageColor = color; this.messageTimer = System.nanoTime();
    }
    private void spawnFloatingText(String text, int gridX, int gridY, Color color) {
        floatingTexts.add(new FloatingText(text, gridX * CELL, gridY * CELL, color));
    }
    private void drawFloatingText(GraphicsContext gc) {
        if (floatingTexts.isEmpty()) return;
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        Iterator<FloatingText> it = floatingTexts.iterator();
        while (it.hasNext()) {
            FloatingText ft = it.next();
            gc.setFill(ft.color);
            gc.setGlobalAlpha(ft.life); 
            gc.fillText(ft.text, ft.x, ft.y);
            ft.y -= 0.5; ft.life -= 0.02; 
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

                    // --- NEW: SET LIFE BASED ON NAME ---
                    double baseLives = 3.0; // Default for unknown enemies
                    
                    if (name.equalsIgnoreCase("Koura") || name.equalsIgnoreCase("Sark")) {
                        baseLives = 1.0; // Die in 1 hit
                    } 
                    else if (name.equalsIgnoreCase("Rinzler") || name.equalsIgnoreCase("Clu")) {
                        baseLives = 2.0; // Die in 2 hits
                    }

                    // Use 'baseLives' instead of 3.0
                    enemyPool.add(new Enemy(name, color, baseLives, speed, data[2].trim(), xp, data[5].trim()));
                }
            }
            scanner.close();
        } catch (Exception e) { e.printStackTrace(); }
    }
}